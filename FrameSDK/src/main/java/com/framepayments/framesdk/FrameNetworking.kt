package com.framepayments.framesdk
import android.content.Context
import com.evervault.sdk.Evervault
import com.framepayments.framesdk.configurations.ConfigurationAPI
import com.framepayments.framesdk.configurations.ConfigurationResponses
import com.framepayments.framesdk.configurations.SecureConfigurationStorage
import com.framepayments.framesdk.managers.SiftManager
import com.framepayments.framesdk.fingerprint.FingerprintManager
import com.framepayments.framesdk.sonar.SessionManager as SonarSessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class DefaultURLSession(private val client: OkHttpClient) : URLSessionProtocol {
    override suspend fun execute(request: Request): Response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }
}

object FrameNetworking {
    val gson: Gson = Gson()

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }
    var asyncURLSession: URLSessionProtocol = DefaultURLSession(okHttpClient)
    var mainApiUrl: String = NetworkingConstants.MAIN_API_URL
    const val CURRENT_VERSION = BuildConfig.SDK_VERSION
    var apiKey: String = ""
    var debugMode: Boolean = false
    var isEvervaultConfigured: Boolean = false
    private var sonarSessionManager: SonarSessionManager? = null

    private lateinit var applicationContext: Context

    fun initializeWithAPIKey(context: Context, key: String, debug: Boolean = false) {
        apiKey = key
        debugMode = debug
        applicationContext = context.applicationContext

        SiftManager.initializeSift(apiKey)
        configureEvervault()

        // Initialize Sonar session as early as possible during SDK initialization
        // using Fingerprint visitorId when available. If we cannot obtain a
        // Fingerprint visitorId, we skip Sonar session initialization to match
        // the iOS behavior.
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            val context = getContext()
            FingerprintManager.getVisitorId(context) { fingerprintVisitorId ->
                val visitorId = fingerprintVisitorId ?: run {
                    if (debugMode) {
                        println("Fingerprint visitorId is null; skipping Sonar session initialization.")
                    }
                    return@getVisitorId
                }

                kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val manager = SonarSessionManager.initializeWithFrameNetworking(getContext(), visitorId)
                        sonarSessionManager = manager
                    } catch (e: Exception) {
                        if (debugMode) {
                            println("Failed to initialize Sonar session: $e")
                        }
                    }
                }
            }
        }
    }

    fun currentSonarSessionId(): String? = sonarSessionManager?.getSessionId()

    fun getContext(): Context {
        check(::applicationContext.isInitialized) { "FrameSDK must be initialized before use" }
        return applicationContext
    }

    private fun describeServerError(response: Response, bodyBytes: ByteArray?): String {
        val fromBody = bodyBytes?.let { String(it, Charsets.UTF_8) }?.trim()?.takeIf { it.isNotEmpty() }
        return fromBody ?: response.message.ifEmpty { "HTTP ${response.code}" }
    }

    private fun Request.Builder.withFrameHeaders(): Request.Builder {
        header("Authorization", "Bearer $apiKey")
        header("User-Agent", "Android/$CURRENT_VERSION")
        SiftManager.getIPAddress()?.let { header("ip_address", it) }
        return this
    }

    inline fun <reified T> parseResponse(data: ByteArray?): T? {
        if (data == null) return null
        return try {
            val jsonString = String(data, Charsets.UTF_8)
            val type = object : TypeToken<T>() {}.type
            gson.fromJson(jsonString, type)
        } catch (_: Exception) {
            null
        }
    }

    inline fun <reified T> parseListResponse(data: ByteArray?): List<T>? {
        if (data == null) return null
        return try {
            val jsonString = String(data, Charsets.UTF_8)
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson<List<T>>(jsonString, type)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun performDataTask(
        endpoint: FrameNetworkingEndpoints
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl
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
            .withFrameHeaders()

        val method = endpoint.httpMethod.uppercase()
        requestBuilder.method(method, null)
        val request = requestBuilder.build()

        return try {
            val response = asyncURLSession.execute(request)
            val responseData = response.body?.bytes()
            if (debugMode) {
                println("API Endpoint: ${response.request.url}")
                printDataForTesting(responseData)
            }
            if (!response.isSuccessful) {
                Pair(responseData, NetworkingError.ServerError(response.code, describeServerError(response, responseData)))
            } else {
                Pair(responseData, null)
            }
        } catch (_: UnknownHostException) {
            Pair(null, NetworkingError.InvalidURL)
        } catch (_: IOException) {
            Pair(null, NetworkingError.UnknownError)
        } catch (_: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    suspend fun performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: Any? = null
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl
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
            .withFrameHeaders()

        val requestBody: ByteArray? = try {
            gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }

        val method = endpoint.httpMethod.uppercase()
        if (method == "POST" || method == "PATCH") {
            requestBuilder.header("Content-Type", "application/json")
            val mediaType = "application/json".toMediaTypeOrNull()
            val body = requestBody?.toRequestBody(mediaType)
            requestBuilder.method(method, body)
        } else {
            requestBuilder.method(method, null)
        }
        val request = requestBuilder.build()

        return try {
            val response = asyncURLSession.execute(request)
            val responseData = response.body?.bytes()
            if (debugMode) {
                println("API Endpoint: ${response.request.url}")
                printDataForTesting(requestBody)
                printDataForTesting(responseData)
            }
            if (!response.isSuccessful) {
                Pair(responseData, NetworkingError.ServerError(response.code, describeServerError(response, responseData)))
            } else {
                Pair(responseData, null)
            }
        } catch (_: UnknownHostException) {
            Pair(null, NetworkingError.InvalidURL)
        } catch (_: IOException) {
            Pair(null, NetworkingError.UnknownError)
        } catch (_: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    suspend fun performMultipartDataTask(
        endpoint: FrameNetworkingEndpoints,
        filesToUpload: List<FileUpload>
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl
        val fullUrl = baseUrl + endpoint.endpointURL

        val httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return Pair(null, NetworkingError.InvalidURL)

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (file in filesToUpload) {
            val bytes = file.toByteArray()
            multipartBuilder.addFormDataPart(
                file.fieldName.value,
                file.fileName,
                bytes.toRequestBody(file.mimeType.toMediaTypeOrNull())
            )
        }
        val multipartBody = multipartBuilder.build()

        val request = Request.Builder()
            .url(httpUrl)
            .withFrameHeaders()
            .post(multipartBody)
            .build()

        return try {
            val response = asyncURLSession.execute(request)
            val responseData = response.body?.bytes()
            if (debugMode) {
                println("API Endpoint: ${response.request.url}")
                printDataForTesting(responseData)
            }
            if (!response.isSuccessful) {
                Pair(responseData, NetworkingError.ServerError(response.code, describeServerError(response, responseData)))
            } else {
                Pair(responseData, null)
            }
        } catch (_: java.net.UnknownHostException) {
            Pair(null, NetworkingError.InvalidURL)
        } catch (_: java.io.IOException) {
            Pair(null, NetworkingError.UnknownError)
        } catch (_: Exception) {
            Pair(null, NetworkingError.UnknownError)
        }
    }

    fun performMultipartDataTask(
        endpoint: FrameNetworkingEndpoints,
        filesToUpload: List<FileUpload>,
        completion: (data: ByteArray?, error: NetworkingError?) -> Unit
    ) {
        val baseUrl = NetworkingConstants.MAIN_API_URL
        val fullUrl = baseUrl + endpoint.endpointURL

        val httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return completion(null, NetworkingError.InvalidURL)

        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        for (file in filesToUpload) {
            val bytes = file.toByteArray()
            multipartBuilder.addFormDataPart(
                file.fieldName.value,
                file.fileName,
                bytes.toRequestBody(file.mimeType.toMediaTypeOrNull())
            )
        }
        val multipartBody = multipartBuilder.build()

        val request = Request.Builder()
            .url(httpUrl)
            .withFrameHeaders()
            .post(multipartBody)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                completion(null, NetworkingError.UnknownError)
            }

            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body?.bytes()
                val error = if (!response.isSuccessful) {
                    NetworkingError.ServerError(response.code, describeServerError(response, bytes))
                } else null
                completion(bytes, error)
            }
        })
    }

    fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
        completion: (data: ByteArray?, error: NetworkingError?) -> Unit
    ) {
        val baseUrl = NetworkingConstants.MAIN_API_URL
        val fullUrl = baseUrl + endpoint.endpointURL

        var httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return completion(null, NetworkingError.InvalidURL)
        endpoint.queryItems?.let { queryItems ->
            val urlBuilder = httpUrl.newBuilder()
            for (item in queryItems) {
                urlBuilder.addQueryParameter(item.name, item.value)
            }
            httpUrl = urlBuilder.build()
        }

        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .withFrameHeaders()

        val method = endpoint.httpMethod.uppercase()
        requestBuilder.method(method, null)
        val request = requestBuilder.build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, NetworkingError.UnknownError)
            }

            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body?.bytes()
                val error = if (!response.isSuccessful) {
                    NetworkingError.ServerError(response.code, describeServerError(response, bytes))
                } else null
                completion(bytes, error)
            }
        })
    }

    fun <T> performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: T? = null,
        completion: (data: ByteArray?, error: NetworkingError?) -> Unit
    ) {
        val baseUrl = NetworkingConstants.MAIN_API_URL
        val fullUrl = baseUrl + endpoint.endpointURL

        var httpUrl: HttpUrl = fullUrl.toHttpUrlOrNull() ?: return completion(null, NetworkingError.InvalidURL)
        endpoint.queryItems?.let { queryItems ->
            val urlBuilder = httpUrl.newBuilder()
            for (item in queryItems) {
                urlBuilder.addQueryParameter(item.name, item.value)
            }
            httpUrl = urlBuilder.build()
        }

        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .withFrameHeaders()

        val requestBody: ByteArray? = try {
            gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }

        val method = endpoint.httpMethod.uppercase()
        if (method == "POST" || method == "PATCH") {
            requestBuilder.header("Content-Type", "application/json")
            val mediaType = "application/json".toMediaTypeOrNull()
            val body = requestBody?.toRequestBody(mediaType)
            requestBuilder.method(method, body)
        } else {
            requestBuilder.method(method, null)
        }
        val request = requestBuilder.build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                completion(null, NetworkingError.UnknownError)
            }

            override fun onResponse(call: Call, response: Response) {
                val bytes = response.body?.bytes()
                val error = if (!response.isSuccessful) {
                    NetworkingError.ServerError(response.code, describeServerError(response, bytes))
                } else null
                completion(bytes, error)
            }
        })
    }

    private fun printDataForTesting(data: ByteArray?) {
        data?.let {
            val jsonString = String(it)
            println(jsonString)
        }
    }

    fun configureEvervault() {
        val config: ConfigurationResponses.GetEvervaultConfigurationResponse? = SecureConfigurationStorage.retrieve(getContext(), "evervault")
        if (config == null) {
            ConfigurationAPI.getEvervaultConfiguration { configFromAPI ->
                Evervault.shared.configure(configFromAPI?.teamId ?: "", configFromAPI?.appId ?: "")
                isEvervaultConfigured = true
            }
        } else {
            Evervault.shared.configure(config.teamId ?: "", config.appId ?: "")
            isEvervaultConfigured = true
        }
    }
}
