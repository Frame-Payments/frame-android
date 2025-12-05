package com.framepayments.framesdk

import com.framepayments.framesdk.invoicelineitems.InvoiceLineItemRequests
import com.framepayments.framesdk.invoicelineitems.InvoiceLineItemsAPI
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InvoiceLineItemsAPITestAPITest {
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
    fun testCreateInvoiceLineItem() = runBlocking {
        val responseBody = """{"id":"item_123", "object":"invoice_line_item", "quantity":20}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = InvoiceLineItemRequests.CreateLineItemRequest(
            product = "prod_123",
            quantity = 20
        )
        val (result, error) = InvoiceLineItemsAPI.createInvoiceLineItem("inv_123", request)

        assertNotNull(result)
        assertEquals("item_123", result?.id)
        assertEquals(20, result?.quantity)
    }

    @Test
    fun testUpdateInvoiceLineItem() = runBlocking {
        val responseBody = """{"id":"item_123", "object":"invoice_line_item", "quantity":12}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = InvoiceLineItemRequests.UpdateLineItemRequest(
            product = null,
            quantity = 12
        )
        val (result, error) = InvoiceLineItemsAPI.updateInvoiceLineItem("inv_123", "item_123", request)

        assertNotNull(result)
        assertEquals("item_123", result?.id)
        assertEquals(12, result?.quantity)
    }

    @Test
    fun testGetInvoiceLineItemWithId() = runBlocking {
        val responseBody = """{"id":"item_123", "object":"invoice_line_item", "quantity":20}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoiceLineItemsAPI.getInvoiceLineItemWith("inv_123", "item_123")

        assertNotNull(result)
        assertEquals("item_123", result?.id)
        assertEquals(20, result?.quantity)
    }

    @Test
    fun testGetInvoiceLineItems() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"item_123", "object":"invoice_line_item", "quantity":20},
                    {"id":"item_234", "object":"invoice_line_item", "quantity":12}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoiceLineItemsAPI.getInvoiceLineItems("inv_123")

        assertNotNull(result)
        assertEquals("item_123", result?.data?.get(0)?.id)
        assertEquals(12, result?.data?.get(1)?.quantity)
    }
    @Test
    fun testDeleteInvoiceLineItemWithId() = runBlocking {
        val responseBody = """{"object":"invoice_line_item", "deleted": true}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = InvoiceLineItemsAPI.deleteInvoiceLineItem("inv_123", "item_123")

        assertNotNull(result)
        assertEquals("invoice_line_item", result?.deletedObject)
    }
}