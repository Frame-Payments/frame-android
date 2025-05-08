package com.framepayments.framesdk
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class DefaultURLSession : URLSessionProtocol {
    private val client = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    override suspend fun execute(request: Request): Response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }
}

object FrameNetworking {
    val gson: Gson = Gson()
    var asyncURLSession: URLSessionProtocol = DefaultURLSession()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
    const val currentVersion = BuildConfig.SDK_VERSION
    var apiKey: String = ""
    var debugMode: Boolean = false

    fun initializeWithAPIKey(key: String, debug: Boolean = false) {
        apiKey = key
        debugMode = debug
    }

    inline fun <reified T> parseResponse(data: ByteArray?): T? {
        if (data == null) return null

        return try {
            val jsonString = String(data, Charsets.UTF_8)
            gson.fromJson(jsonString, T::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun performDataTask(
        endpoint: FrameNetworkingEndpoints
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
            .header("User-Agent", "Android/$currentVersion")

        val method = endpoint.httpMethod.uppercase()
        requestBuilder.method(method, null)
        val request = requestBuilder.build()

        return try {
            val response = asyncURLSession.execute(request)
            if (debugMode) {
                println("API Endpoint: ${response.request.url}")
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
            Pair(null, NetworkingError.UnknownError)
        } catch (e: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    internal suspend inline fun <reified T> performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: T? = null
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
            .header("User-Agent", "Android/$currentVersion")

        val requestBody: ByteArray? = try {
            gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

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
            Pair(null, NetworkingError.UnknownError)
        } catch (e: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
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
            .header("User-Agent", "Android/$currentVersion")

        val method = endpoint.httpMethod.uppercase()
        requestBuilder.method(method, null)
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

    inline fun <reified T> performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: T? = null,
        crossinline completion: (data: ByteArray?, response: Response?, error: Exception?) -> Unit
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
            .header("User-Agent", "Android/$currentVersion")

        val requestBody: ByteArray? = try {
            gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }

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