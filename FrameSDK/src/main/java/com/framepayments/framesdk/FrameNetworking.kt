package com.framepayments.framesdk
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.UnknownHostException

class DefaultURLSession : URLSessionProtocol {
    private val client = OkHttpClient()

    override suspend fun execute(request: Request): Response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }
}

object FrameNetworking {
    private val gson: Gson = Gson()

    var asyncURLSession: URLSessionProtocol = DefaultURLSession()

    private val okHttpClient: OkHttpClient = OkHttpClient()
    private var apiKey: String = ""
    private var debugMode: Boolean = false

    fun initializeWithAPIKey(key: String, debug: Boolean = false) {
        apiKey = key
        debugMode = debug
    }

    suspend fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
        requestBody: ByteArray? = null
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = NetworkingConstants.MAIN_API_URL
        val fullUrl = baseUrl + endpoint.endpointURL

        var httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return Pair(null, NetworkingError.InvalidURL)

        endpoint.queryItems?.let { queryItems ->
            val urlBuilder = httpUrl.newBuilder()
            for (item in queryItems) {
                urlBuilder.addQueryParameter(item.name, item.value)
            }
            httpUrl = urlBuilder.build()
        }

        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("User-Agent", "Android")

        val method = endpoint.httpMethod.uppercase()
        if (method == "POST" || method == "PATCH") {
            requestBuilder.header("Content-Type", "application/json")
            val mediaType = "application/json".toMediaTypeOrNull()
            val body = requestBody?.let { RequestBody.create(mediaType, it) }
            requestBuilder.method(method, body)
        } else {
            requestBuilder.method(method, null)
        }
        val request = requestBuilder.build()

        return try {
            // Execute the request using our asyncURLSession.
            val response = asyncURLSession.execute(request)
            if (debugMode) {
                println("API Endpoint: ${response.request.url}")
                printDataForTesting(requestBody)
                val responseData = response.body?.bytes()
                printDataForTesting(responseData)
            }
            if (!response.isSuccessful) {
                Pair(null, NetworkingError.ServerError(response.code))
            } else {
                val data = response.body?.bytes()
                Pair(data, null)
            }
        } catch (e: UnknownHostException) {
            Pair(null, NetworkingError.InvalidURL)
        } catch (e: IOException) {
            // Depending on the error, you could distinguish decoding issues.
            Pair(null, NetworkingError.UnknownError)
        } catch (e: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
        requestBody: ByteArray? = null,
        completion: (data: ByteArray?, response: Response?, error: Exception?) -> Unit
    ) {
        val baseUrl = NetworkingConstants.MAIN_API_URL
        val fullUrl = baseUrl + endpoint.endpointURL

        var httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return completion(null, null, IllegalArgumentException("Invalid URL"))
        endpoint.queryItems?.let { queryItems ->
            val urlBuilder = httpUrl.newBuilder()
            for (item in queryItems) {
                urlBuilder.addQueryParameter(item.name, item.value)
            }
            httpUrl = urlBuilder.build()
        }

        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .header("Authorization", "Bearer $apiKey")
            .header("User-Agent", "Android")

        val method = endpoint.httpMethod.uppercase()
        if (method == "POST" || method == "PATCH") {
            requestBuilder.header("Content-Type", "application/json")
            val mediaType = "application/json".toMediaTypeOrNull()
            val body = requestBody?.let { RequestBody.create(mediaType, it) }
            requestBuilder.method(method, body)
        } else {
            requestBuilder.method(method, null)
        }
        val request = requestBuilder.build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                completion(response.body?.bytes(), response, null)
            }
        })
    }

    private fun printDataForTesting(data: ByteArray?) {
        data?.let {
            val jsonString = String(it)
            println(jsonString)
        }
    }
}