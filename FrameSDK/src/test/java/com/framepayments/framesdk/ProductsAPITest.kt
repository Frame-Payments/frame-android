package com.framepayments.framesdk

import com.framepayments.framesdk.products.ProductPurchaseType
import com.framepayments.framesdk.products.ProductsAPI
import com.framepayments.framesdk.products.ProductsRequests
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ProductsAPITest {
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
    fun testCreateProduct() = runBlocking {
        val responseBody = """{"id":"prod_123", "name":"test product", "description": "New Product", "shippable": true, "livemode": true, "default_price":100}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ProductsRequests.CreateProductRequest(
            name = "test product",
            description = "New Product",
            defaultPrice = 100,
            purchaseType = ProductPurchaseType.ONE_TIME,
            recurringInterval = null,
            metadata = null,
            shippable = true,
            url = null
        )
        val (result, error) = ProductsAPI.createProduct(request,)

        assertNotNull(result)
        assertEquals("prod_123", result?.id)
        assertEquals("test product", result?.name)
    }

    @Test
    fun testUpdateProduct() = runBlocking {
        val responseBody = """{"id":"prod_123", "name":"test product", "description": "Updated Product", "shippable": true, "livemode": true, "default_price":150}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val request = ProductsRequests.UpdateProductRequest(
            name = "test product",
            description = "Updated Product",
            defaultPrice = 150,
            metadata = null,
            shippable = true,
            url = null
        )
        val (result, error) = ProductsAPI.updateProduct("cus_123", request)

        assertNotNull(result)
        assertEquals("Updated Product", result?.description)
        assertEquals(150, result?.defaultPrice)
    }

    @Test
    fun testGetProductWithId() = runBlocking {
        val responseBody = """{"id":"prod_123", "name":"test product", "description": "New Product", "shippable": true, "livemode": true, "default_price":100}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductsAPI.getProductWith("cus_999")

        assertNotNull(result)
        assertEquals("test product", result?.name)
        assertEquals("New Product", result?.description)
    }

    @Test
    fun testGetProducts() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"prod_123", "name":"test product", "description": "New Product", "shippable": false, "livemode": true, "default_price":100},
                    {"id":"prod_234", "name":"test product 2", "description": "Updated Product", "shippable": true, "livemode": true, "default_price":150}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductsAPI.getProducts(0, 100)

        assertNotNull(result)
        assertEquals("prod_123", result?.data?.get(0)?.id)
        assertEquals("Updated Product", result?.data?.get(1)?.description)
        assertEquals("test product 2", result?.data?.get(1)?.name)
    }

    @Test
    fun testSearchProducts() = runBlocking {
        val responseBody = """
            {
                "data": [
                    {"id":"prod_234", "name":"test product 2", "description": "Updated Product", "shippable": true, "livemode": true, "default_price":150}
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductsAPI.searchProducts(null, null, true)

        assertNotNull(result)
        assertEquals("prod_234", result?.get(0)?.id)
        assertEquals("test product 2", result?.get(0)?.name)
    }

    @Test
    fun testDeleteProductWithId() = runBlocking {
        val responseBody = """{"object":"product", "deleted": true, "id":"prod_123"}"""
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        val (result, error) = ProductsAPI.deleteProduct("cus_999")

        assertNotNull(result)
        assertEquals("prod_123", result?.id)
        assertEquals(true, result?.deleted)
    }
}