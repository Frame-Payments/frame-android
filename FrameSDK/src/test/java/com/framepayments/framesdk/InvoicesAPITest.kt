package com.framepayments.framesdk

import com.framepayments.framesdk.invoices.InvoiceCollectionMethod
import com.framepayments.framesdk.invoices.InvoicesAPI
import com.framepayments.framesdk.invoices.InvoiceRequests
import com.framepayments.framesdk.invoices.InvoiceStatus
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InvoicesAPITest {
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        FrameNetworking.mainApiUrl = mockWebServer.url("/").toString()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testCreateInvoiceWithCustomer() = runBlocking {
        val responseBody = """{"net_terms":14, "description":"new invoice"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = InvoiceRequests.CreateInvoiceRequest(
            collectionMethod = InvoiceCollectionMethod.AUTO_CHARGE,
            netTerms = 14,
            lineItems = null,
            customer = "cus_123",
            number = "1",
            description = "new invoice",
            memo = null,
            metadata = null
        )
        val (result, error) = InvoicesAPI.createInvoice(request)

        assertNotNull(result)
        assertEquals(14, result?.netTerms)
        assertEquals("new invoice", result?.invoiceDescription)
    }

    @Test
    fun testCreateInvoiceWithAccount() = runBlocking {
        val responseBody = """{"net_terms":30, "description":"account invoice"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = InvoiceRequests.CreateInvoiceRequest(
            collectionMethod = InvoiceCollectionMethod.REQUEST_PAYMENT,
            netTerms = 30,
            lineItems = null,
            account = "acc_123",
            dueDate = "2026-04-30",
            number = null,
            description = "account invoice",
            memo = null,
            metadata = null
        )
        val (result, error) = InvoicesAPI.createInvoice(request)

        assertNotNull(result)
        assertEquals(30, result?.netTerms)
    }

    @Test
    fun testUpdateInvoice() = runBlocking {
        val responseBody = """{"id":"inv_123", "collection_method":"request_payment"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = InvoiceRequests.UpdateInvoiceRequest(
            collectionMethod = InvoiceCollectionMethod.REQUEST_PAYMENT,
            description = "updated invoice"
        )
        val (result, error) = InvoicesAPI.updateInvoice("inv_123", request)

        assertNotNull(result)
        assertEquals(InvoiceCollectionMethod.REQUEST_PAYMENT, result?.collectionMethod)
    }

    @Test
    fun testGetInvoiceWithId() = runBlocking {
        val responseBody = """{"net_terms":14, "description":"new invoice"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoicesAPI.getInvoiceWith("inv_123")

        assertNotNull(result)
        assertEquals(14, result?.netTerms)
        assertEquals("new invoice", result?.invoiceDescription)
    }

    @Test
    fun testGetInvoices() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"inv_123", "net_terms":14, "description":"new invoice"},
                    {"id":"inv_234", "net_terms":10, "description":"updated invoice"}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoicesAPI.getInvoices(page = 0, perPage = 100)

        assertNotNull(result)
        assertEquals("inv_123", result?.data?.get(0)?.id)
        assertEquals("updated invoice", result?.data?.get(1)?.invoiceDescription)
    }

    @Test
    fun testGetInvoicesFilteredByAccount() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"inv_300", "net_terms":7, "description":"account invoice"}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoicesAPI.getInvoices(account = "acc_123")

        assertNotNull(result)
        assertEquals("inv_300", result?.data?.get(0)?.id)

        val recorded = mockWebServer.takeRequest()
        assertEquals("acc_123", recorded.requestUrl?.queryParameter("account"))
    }

    @Test
    fun testDeleteInvoiceWithId() = runBlocking {
        val responseBody = """{"object":"invoice", "deleted": true}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoicesAPI.deleteInvoice("inv_123")

        assertNotNull(result)
        assertEquals("invoice", result?.deletedObject)
    }

    @Test
    fun testIssueInvoice() = runBlocking {
        val responseBody = """{"net_terms":14, "description":"new invoice", "status":"paid"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoicesAPI.issueInvoice("inv_123")

        assertNotNull(result)
        assertEquals(InvoiceStatus.PAID, result?.status)
    }
}