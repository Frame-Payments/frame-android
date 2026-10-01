package com.framepayments.framesdk

import com.framepayments.framesdk.transfersv2.FrameTransferV2Error
import com.framepayments.framesdk.transfersv2.TransferV2ClientSecret
import com.framepayments.framesdk.transfersv2.TransferV2Requests
import com.framepayments.framesdk.transfersv2.TransferV2Endpoints
import com.framepayments.framesdk.transfersv2.TransferV2Status
import com.framepayments.framesdk.transfersv2.TransferV2Type
import com.framepayments.framesdk.transfersv2.TransfersV2API
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TransfersV2APITest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
        FrameNetworking.apiSecretKey = "sk_test_key"
        FrameNetworking.apiPublishableKey = "pk_test_key"
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun makeCreateRequest() = TransferV2Requests.CreateTransferRequest(
        amount = TransferV2Requests.MoneyAmount(value = 10000, currency = "usd"),
        source = TransferV2Requests.EndpointSlot(
            accountId = "acc_123",
            paymentMethodId = "pm_123"
        ),
        confirm = false,
        authorizationMode = "automatic"
    )

    @Test
    fun testCreateTransfer() = runBlocking {
        val responseBody = """
            {
              "id":"tr_v2_1",
              "object":"transfer",
              "type":"payment",
              "status":"pending",
              "amount":{"value":10000,"currency":"usd"},
              "fee":{"value":30,"currency":"usd"},
              "net_amount":{"value":9970,"currency":"usd"}
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = makeCreateRequest()
        val (result, _) = TransfersV2API.createTransfer(request, idempotencyKey = "key-1")

        assertNotNull(result)
        assertEquals("tr_v2_1", result?.id)
        assertEquals(TransferV2Status.PENDING, result?.status)
        assertEquals(TransferV2Type.PAYMENT, result?.type)
        assertEquals(10000, result?.amount?.value)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.endsWith("/v2/transfers"))
        assertEquals("key-1", recorded.getHeader("Idempotency-Key"))
        val body = recorded.body.readUtf8()
        assertTrue(body.contains("\"value\":10000"))
        assertTrue(body.contains("payment_method_id"))
        assertTrue(body.contains("\"confirm\":false"))
    }

    @Test
    fun testCreateTransferGeneratesIdempotencyKey() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"tr_v2_2","status":"pending","type":"payment"}""")
        )

        TransfersV2API.createTransfer(makeCreateRequest())

        val recorded = mockWebServer.takeRequest()
        val key = recorded.getHeader("Idempotency-Key")
        assertNotNull(key)
        assertTrue(key!!.isNotEmpty())
    }

    @Test
    fun testCreateTransferIdempotencyHeaderOnEndpoint() {
        val endpoint = TransferV2Endpoints.CreateTransfer(idempotencyKey = "abc-123")
        assertEquals("abc-123", endpoint.additionalHeaders["Idempotency-Key"])
        assertEquals("/v2/transfers", endpoint.endpointURL)
        assertEquals("POST", endpoint.httpMethod)
    }

    @Test
    fun testMemberEndpoints() {
        val confirm = TransferV2Endpoints.ConfirmTransfer("tr_1", "k1")
        assertEquals("/v2/transfers/tr_1/confirm", confirm.endpointURL)
        assertEquals("k1", confirm.additionalHeaders["Idempotency-Key"])
        assertTrue(TransferV2Endpoints.ConfirmTransfer("tr_1", null).additionalHeaders.isEmpty())

        val capture = TransferV2Endpoints.CaptureTransfer("tr_1", "k2")
        assertEquals("POST", capture.httpMethod)
        assertEquals("k2", capture.additionalHeaders["Idempotency-Key"])

        assertEquals("PATCH", TransferV2Endpoints.UpdateTransfer("tr_1").httpMethod)
        assertEquals(
            "/v2/transfers/tr_1/void",
            TransferV2Endpoints.VoidTransfer("tr_1", "k3").endpointURL
        )
        assertEquals(
            "/v2/transfers/tr_1/refund",
            TransferV2Endpoints.RefundTransfer("tr_1", "k4").endpointURL
        )
    }

    @Test
    fun testGetTransfersList() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"tr_1", "status":"pending", "type":"payment", "amount":{"value":100,"currency":"usd"}},
                    {"id":"tr_2", "status":"completed", "type":"payout", "amount":{"value":200,"currency":"usd"}}
                ]
            }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersV2API.getTransfers(perPage = 10, page = 1)

        assertNotNull(result)
        assertEquals(2, result?.data?.size)
        assertEquals("tr_2", result?.data?.get(1)?.id)
        assertEquals(TransferV2Status.COMPLETED, result?.data?.get(1)?.status)
        assertEquals(TransferV2Type.PAYOUT, result?.data?.get(1)?.type)

        val recorded = mockWebServer.takeRequest()
        assertTrue(recorded.path!!.contains("per_page=10"))
        assertTrue(recorded.path!!.contains("page=1"))
    }

    @Test
    fun testGetTransferWithId() = runBlocking {
        val responseBody = """{"id":"tr_4","status":"completed","type":"payment","amount":{"value":777,"currency":"usd"}}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersV2API.getTransferWith("tr_4")

        assertNotNull(result)
        assertEquals("tr_4", result?.id)
        assertEquals(TransferV2Status.COMPLETED, result?.status)
        assertEquals(777, result?.amount?.value)
    }

    @Test
    fun testGetTransferWithEmptyId() = runBlocking {
        val (result, error) = TransfersV2API.getTransferWith("")
        assertNull(result)
        assertNull(error)
    }

    @Test
    fun testCreateTransferReturnsServerError() = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"error":"idempotency_key_required"}""")
        )

        val (_, error) = TransfersV2API.createTransfer(makeCreateRequest())

        assertNotNull(error)
        when (val e = error) {
            is NetworkingError.ServerError -> assertEquals(400, e.statusCode)
            else -> throw AssertionError("Expected NetworkingError.ServerError, got $e")
        }
    }

    @Test
    fun testTransferV2DecodesNestedMoneyFromBackendPayload() {
        val json = """
        {
          "id":"tr_v2_1",
          "object":"transfer",
          "type":"payment",
          "status":"pending",
          "amount":{"value":15000,"currency":"usd"},
          "fee":{"value":45,"currency":"usd"},
          "net_amount":{"value":14955,"currency":"usd"},
          "payment":{"status":"succeeded","authorization_mode":"automatic"},
          "client_secret":"tr_v2_1_secret_abc",
          "next_action":{
            "type":"use_frame_sdk",
            "redirect_url":"https://example.com/3ds",
            "use_frame_sdk":{"source":"sess_1","challenge_url":"https://example.com/challenge"}
          }
        }
        """.trimIndent()

        val transfer = FrameNetworking.parseResponse<com.framepayments.framesdk.transfersv2.TransferV2>(
            json.toByteArray(Charsets.UTF_8)
        )
        assertNotNull(transfer)
        assertEquals(15000, transfer?.amount?.value)
        assertEquals("succeeded", transfer?.payment?.status)
        assertEquals(TransferV2Type.PAYMENT, transfer?.type)
        assertEquals("tr_v2_1_secret_abc", transfer?.clientSecret)
        assertEquals("use_frame_sdk", transfer?.nextAction?.type)
        assertEquals("https://example.com/3ds", transfer?.nextAction?.redirectUrl)
        assertEquals("sess_1", transfer?.nextAction?.useFrameSDK?.source)
        assertEquals("https://example.com/challenge", transfer?.nextAction?.useFrameSDK?.challengeUrl)
    }

    @Test
    fun testUnknownStatusMapsToUnknown() {
        val json = """{"id":"tr_x","status":"brand_new_status","type":"weird_type"}"""
        val transfer = FrameNetworking.parseResponse<com.framepayments.framesdk.transfersv2.TransferV2>(
            json.toByteArray(Charsets.UTF_8)
        )
        assertNotNull(transfer)
        assertEquals(TransferV2Status.UNKNOWN, transfer?.status)
        assertEquals(TransferV2Type.UNKNOWN, transfer?.type)
    }

    @Test
    fun testConfirmCaptureVoidRefund() = runBlocking {
        val responseBody = """{"id":"tr_v2_1","status":"pending","type":"payment"}"""

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val (confirmed, _) = TransfersV2API.confirmTransfer("tr_v2_1")
        assertEquals("tr_v2_1", confirmed?.id)
        assertEquals("POST", mockWebServer.takeRequest().method)

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val (captured, _) = TransfersV2API.captureTransfer(
            "tr_v2_1",
            TransferV2Requests.AmountOnlyRequest(
                amount = TransferV2Requests.MoneyAmount(value = 5000, currency = "usd")
            )
        )
        assertEquals("tr_v2_1", captured?.id)
        val captureReq = mockWebServer.takeRequest()
        assertEquals("POST", captureReq.method)
        assertTrue(captureReq.path!!.endsWith("/capture"))
        assertTrue(captureReq.body.readUtf8().contains("5000"))

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val (voided, _) = TransfersV2API.voidTransfer("tr_v2_1")
        assertEquals("tr_v2_1", voided?.id)
        assertTrue(mockWebServer.takeRequest().path!!.endsWith("/void"))

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))
        val (refunded, _) = TransfersV2API.refundTransfer("tr_v2_1")
        assertEquals("tr_v2_1", refunded?.id)
        assertTrue(mockWebServer.takeRequest().path!!.endsWith("/refund"))
    }

    @Test
    fun testConfirmTransferWithClientSecretUsesPublishableKeyAndBody() = runBlocking {
        FrameNetworking.apiPublishableKey = "pk_test_confirm"
        val responseBody = """{"id":"tr_v2_1","status":"pending","type":"payment","payment":{"status":"succeeded"}}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersV2API.confirmTransfer("tr_v2_1", "tr_tr_v2_1_secret_abc")

        assertNotNull(result)
        assertEquals("tr_v2_1", result?.id)

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer pk_test_confirm", recorded.getHeader("Authorization"))
        assertTrue(recorded.path!!.endsWith("/v2/transfers/tr_v2_1/confirm"))
        assertTrue(recorded.body.readUtf8().contains("\"client_secret\":\"tr_tr_v2_1_secret_abc\""))
    }

    @Test
    fun testGetTransferWithClientSecretUsesPublishableKey() = runBlocking {
        FrameNetworking.apiPublishableKey = "pk_test_get"
        val responseBody = """{"id":"tr_v2_1","status":"pending","type":"payment"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, _) = TransfersV2API.getTransferWith("tr_v2_1", "tr_tr_v2_1_secret_xyz")

        assertNotNull(result)
        assertEquals("tr_v2_1", result?.id)

        val recorded = mockWebServer.takeRequest()
        assertEquals("Bearer pk_test_get", recorded.getHeader("Authorization"))
        assertTrue(recorded.path!!.endsWith("/v2/transfers/tr_v2_1"))
    }

    @Test
    fun testTransferV2ClientSecretParsesTrPrefix() {
        val secret = TransferV2ClientSecret("tr_v2_1_secret_tok")
        assertEquals("tr_v2_1_secret_tok", secret.value)
        assertEquals("v2_1", secret.transferId)
    }

    @Test
    fun testTransferV2ClientSecretParsesCiBridge() {
        val secret = TransferV2ClientSecret("ci_intent_123_secret_abc")
        assertEquals("intent_123", secret.transferId)
    }

    @Test
    fun testTransferV2ClientSecretRejectsInvalid() {
        try {
            TransferV2ClientSecret("not_a_secret")
            fail("Expected InvalidClientSecret")
        } catch (_: FrameTransferV2Error.InvalidClientSecret) {
            // expected
        }

        try {
            TransferV2ClientSecret("tr_only")
            fail("Expected InvalidClientSecret")
        } catch (_: FrameTransferV2Error.InvalidClientSecret) {
            // expected
        }

        try {
            TransferV2ClientSecret("tr_id_secret_")
            fail("Expected InvalidClientSecret")
        } catch (_: FrameTransferV2Error.InvalidClientSecret) {
            // expected
        }
    }
}
