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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

/** Default [URLSessionProtocol] implementation backed by OkHttp. */
class DefaultURLSession(private val client: OkHttpClient) : URLSessionProtocol {
    override suspend fun execute(request: Request): Response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }
}

/**
 * Central SDK singleton. Initialize once with [initializeWithAPIKey] from your `Application.onCreate`
 * before using any Frame UI component or API client.
 *
 * @sample
 * ```kotlin
 * FrameNetworking.initializeWithAPIKey(
 *     context = this,
 *     secretKey = "sk_test_...",
 *     publishableKey = "pk_test_...",
 * )
 * ```
 */
object FrameNetworking {
    /** Gson instance shared across all SDK API clients. */
    val gson: Gson = Gson()

    /** Shared OkHttp client configured with Frame's standard timeouts. */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    /** HTTP session used for all suspend-based API calls. Replace in tests to inject a mock. */
    var asyncURLSession: URLSessionProtocol = DefaultURLSession(okHttpClient)

    /** Base URL for all Frame API requests. Defaults to the production endpoint. */
    var mainApiUrl: String = NetworkingConstants.MAIN_API_URL

    /** The current SDK version string, sourced from `BuildConfig`. */
    const val CURRENT_VERSION = BuildConfig.SDK_VERSION

    /** The secret key set during [initializeWithAPIKey]. Used as the Bearer token for most API calls. */
    var apiSecretKey: String = ""

    /** The publishable key set during [initializeWithAPIKey]. Used for client-safe API calls. */
    var apiPublishableKey: String = ""

    /** When `true`, request URLs, request bodies, and response bodies are printed to logcat. */
    var debugMode: Boolean = false

    /** `true` once Evervault has been successfully configured; `false` until then. */
    var isEvervaultConfigured: Boolean = false

    /**
     * Google Pay merchant identifier, captured at SDK init and read by every Google Pay surface
     * (FrameGooglePayButton, the bundled checkout, onboarding's wallet attach). Null until set.
     */
    var googlePayMerchantId: String? = null
        private set

    private var sonarSessionManager: SonarSessionManager? = null
    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var applicationContext: Context

    /**
     * Initializes the Frame SDK. Call once from `Application.onCreate` before using any SDK component.
     *
     * @param context The application context.
     * @param secretKey Your Frame secret key (`sk_...`). Keep this key server-side where possible.
     * @param publishableKey Your Frame publishable key (`pk_...`). Safe to embed in the app.
     * @param googlePayMerchantId Optional Google Pay merchant identifier. Required to show the Google Pay button in checkout.
     * @param debug When `true`, API requests and responses are logged to logcat.
     */
    fun initializeWithAPIKey(
        context: Context,
        secretKey: String,
        publishableKey: String,
        googlePayMerchantId: String? = null,
        debug: Boolean = false,
    ) {
        apiSecretKey = secretKey
        apiPublishableKey = publishableKey
        this.googlePayMerchantId = googlePayMerchantId
        debugMode = debug
        applicationContext = context.applicationContext

        SiftManager.initializeSift(apiSecretKey)

        // Evervault config first-launch reads `EncryptedSharedPreferences`, which lazily
        // generates an AES master key in the Android Keystore — that takes hundreds of
        // milliseconds on a cold start. Run it off the main thread so app launch isn't
        // blocked. `EncryptedPaymentCardInput` polls `isEvervaultConfigured` before
        // inflating, so a brief delay is safe; pre-Evervault checkout attempts simply
        // wait through that poll.
        sdkScope.launch {
            configureEvervault()
        }

        sdkScope.launch {
            SiftManager.getPublicIp()
        }

        // Initialize Sonar session as early as possible during SDK initialization
        // using Fingerprint visitorId when available. If we cannot obtain a
        // Fingerprint visitorId, we skip Sonar session initialization to match
        // the iOS behavior.
        sdkScope.launch {
            val context = getContext()
            FingerprintManager.getVisitorId(context) { fingerprintVisitorId ->
                val visitorId = fingerprintVisitorId ?: run {
                    if (debugMode) {
                        println("Fingerprint visitorId is null; skipping Sonar session initialization.")
                    }
                    return@getVisitorId
                }

                sdkScope.launch {
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

    /** Returns the current Sonar session identifier, or `null` if Sonar has not been initialized. */
    fun currentSonarSessionId(): String? = sonarSessionManager?.getSessionId()

    /**
     * Returns the application context stored during [initializeWithAPIKey].
     *
     * @throws IllegalStateException if called before [initializeWithAPIKey].
     */
    fun getContext(): Context {
        check(::applicationContext.isInitialized) { "FrameSDK must be initialized before use" }
        return applicationContext
    }

    private fun describeServerError(response: Response, bodyBytes: ByteArray?): String {
        val fromBody = bodyBytes?.let { String(it, Charsets.UTF_8) }?.trim()?.takeIf { it.isNotEmpty() }
        return fromBody ?: response.message.ifEmpty { "HTTP ${response.code}" }
    }

    private fun Request.Builder.applyFrameHeaders(ip: String?, usePublishableKey: Boolean = false): Request.Builder {
        val authKey = if (usePublishableKey && apiPublishableKey.isNotEmpty()) apiPublishableKey else apiSecretKey
        header("Authorization", "Bearer $authKey")
        header("User-Agent", "Android/$CURRENT_VERSION")
        ip?.let { header("ip_address", it) }
        return this
    }

    /**
     * Attaches headers using only the cached public IP. Requests never block waiting
     * on the ipify lookup — the cache is warmed asynchronously by the launch in
     * [initializeWithAPIKey]. The first request after a cold launch goes out without
     * `ip_address`; later requests pick up the header once the cache is populated.
     */
    private fun Request.Builder.withFrameHeaders(usePublishableKey: Boolean = false): Request.Builder {
        return applyFrameHeaders(SiftManager.getCachedIPAddress(), usePublishableKey)
    }

    /** Same cached-IP behavior as [withFrameHeaders]; this overload is called from OkHttp's worker pool. */
    private fun Request.Builder.withFrameHeadersOnWorkerThread(usePublishableKey: Boolean = false): Request.Builder {
        return applyFrameHeaders(SiftManager.getCachedIPAddress(), usePublishableKey)
    }

    /**
     * Deserializes [data] from JSON into type [T], or returns `null` on failure.
     *
     * @param data Raw JSON bytes from a network response.
     * @return The deserialized object, or `null` if [data] is null or parsing fails.
     */
    inline fun <reified T> parseResponse(data: ByteArray?): T? {
        if (data == null) return null
        return try {
            val jsonString = String(data, Charsets.UTF_8)
            val type = object : TypeToken<T>() {}.type
            gson.fromJson(jsonString, type)
        } catch (e: Exception) {
            if (debugMode) println("parseResponse failed for ${T::class.simpleName}: $e")
            null
        }
    }

    /**
     * Deserializes [data] from a JSON array into a list of [T], or returns `null` on failure.
     *
     * @param data Raw JSON bytes from a network response.
     * @return The deserialized list, or `null` if [data] is null or parsing fails.
     */
    inline fun <reified T> parseListResponse(data: ByteArray?): List<T>? {
        if (data == null) return null
        return try {
            val jsonString = String(data, Charsets.UTF_8)
            val type = object : TypeToken<List<T>>() {}.type
            gson.fromJson<List<T>>(jsonString, type)
        } catch (e: Exception) {
            if (debugMode) println("parseListResponse failed for ${T::class.simpleName}: $e")
            null
        }
    }

    /**
     * Executes a GET or DELETE request to [endpoint] and returns the raw response bytes.
     *
     * @param endpoint The endpoint to call.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @return A pair of (response bytes, error). On success the error is `null`; on failure the bytes may contain an error body.
     */
    suspend fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
        usePublishableKey: Boolean = false
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl.trimEnd('/')
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
            .withFrameHeaders(usePublishableKey)

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

    /**
     * Executes a POST or PATCH request to [endpoint], serializing [request] as the JSON body.
     *
     * @param endpoint The endpoint to call.
     * @param request Optional request body. Serialized to JSON via Gson.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @return A pair of (response bytes, error).
     */
    suspend fun performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: Any? = null,
        usePublishableKey: Boolean = false
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl.trimEnd('/')
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
            .withFrameHeaders(usePublishableKey)

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

    /**
     * Executes a multipart POST to [endpoint], uploading each [FileUpload] as a form-data part.
     *
     * @param endpoint The endpoint to call.
     * @param filesToUpload One or more document images to include in the upload.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @return A pair of (response bytes, error).
     */
    suspend fun performMultipartDataTask(
        endpoint: FrameNetworkingEndpoints,
        filesToUpload: List<FileUpload>,
        usePublishableKey: Boolean = false
    ): Pair<ByteArray?, NetworkingError?> {
        val baseUrl = mainApiUrl.trimEnd('/')
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
            .withFrameHeaders(usePublishableKey)
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

    /**
     * Callback-based multipart POST for callers that cannot use coroutines.
     *
     * @param endpoint The endpoint to call.
     * @param filesToUpload One or more document images to include in the upload.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @param completion Called on an OkHttp worker thread with the response bytes and any error.
     */
    fun performMultipartDataTask(
        endpoint: FrameNetworkingEndpoints,
        filesToUpload: List<FileUpload>,
        usePublishableKey: Boolean = false,
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

        okHttpClient.dispatcher.executorService.execute {
            val request = Request.Builder()
                .url(httpUrl)
                .withFrameHeadersOnWorkerThread(usePublishableKey)
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
    }

    /**
     * Callback-based GET/DELETE for callers that cannot use coroutines.
     *
     * @param endpoint The endpoint to call.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @param completion Called on an OkHttp worker thread with the response bytes and any error.
     */
    fun performDataTask(
        endpoint: FrameNetworkingEndpoints,
        usePublishableKey: Boolean = false,
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

        okHttpClient.dispatcher.executorService.execute {
            val requestBuilder = Request.Builder()
                .url(httpUrl)
                .withFrameHeadersOnWorkerThread(usePublishableKey)

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
    }

    /**
     * Callback-based POST/PATCH for callers that cannot use coroutines.
     *
     * @param endpoint The endpoint to call.
     * @param request Optional request body. Serialized to JSON via Gson.
     * @param usePublishableKey When `true`, the publishable key is used instead of the secret key.
     * @param completion Called on an OkHttp worker thread with the response bytes and any error.
     */
    fun <T> performDataTaskWithRequest(
        endpoint: FrameNetworkingEndpoints,
        request: T? = null,
        usePublishableKey: Boolean = false,
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

        val requestBody: ByteArray? = try {
            gson.toJson(request).toByteArray(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }

        okHttpClient.dispatcher.executorService.execute {
            val requestBuilder = Request.Builder()
                .url(httpUrl)
                .withFrameHeadersOnWorkerThread(usePublishableKey)

            val method = endpoint.httpMethod.uppercase()
            if (method == "POST" || method == "PATCH") {
                requestBuilder.header("Content-Type", "application/json")
                val mediaType = "application/json".toMediaTypeOrNull()
                val body = requestBody?.toRequestBody(mediaType)
                requestBuilder.method(method, body)
            } else {
                requestBuilder.method(method, null)
            }
            val builtRequest = requestBuilder.build()

            okHttpClient.newCall(builtRequest).enqueue(object : Callback {
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
    }

    private fun printDataForTesting(data: ByteArray?) {
        data?.let {
            val jsonString = String(it)
            println(jsonString)
        }
    }

    private fun isUsableEvervaultConfig(config: ConfigurationResponses.GetEvervaultConfigurationResponse?): Boolean {
        val team = config?.teamId?.trim().orEmpty()
        val app = config?.appId?.trim().orEmpty()
        return team.isNotEmpty() && app.isNotEmpty()
    }

    /**
     * Applies Evervault credentials when both team id and app id are present.
     * Returns false if config is missing or invalid (do not use Evervault card inputs until this returns true).
     */
    fun applyEvervaultConfiguration(config: ConfigurationResponses.GetEvervaultConfigurationResponse?): Boolean {
        if (!isUsableEvervaultConfig(config)) {
            isEvervaultConfigured = false
            if (debugMode) {
                println("FrameSDK: Evervault not configured (need non-empty team_id and app_id from Frame API).")
            }
            return false
        }
        val team = config!!.teamId!!.trim()
        val app = config.appId!!.trim()
        Evervault.shared.configure(team, app)
        isEvervaultConfigured = true
        return true
    }

    /**
     * Loads Evervault credentials from secure storage or the Frame API (callback-based variant).
     * Prefer [ensureEvervaultReadyForCardInputs] from a coroutine when possible.
     */
    fun configureEvervault() {
        val config: ConfigurationResponses.GetEvervaultConfigurationResponse? =
            SecureConfigurationStorage.retrieve(getContext(), "evervault")
        if (config != null) {
            applyEvervaultConfiguration(config)
            return
        }
        ConfigurationAPI.getEvervaultConfiguration { configFromAPI ->
            applyEvervaultConfiguration(configFromAPI)
        }
    }

    /**
     * Loads Evervault config from secure storage or the Frame API, then applies it.
     * Call from a coroutine before showing Evervault [RowsPaymentCard] / [EncryptedPaymentCardInput].
     */
    suspend fun ensureEvervaultReadyForCardInputs(): Boolean = withContext(Dispatchers.IO) {
        var cfg: ConfigurationResponses.GetEvervaultConfigurationResponse? =
            SecureConfigurationStorage.retrieve(getContext(), "evervault")
        if (!isUsableEvervaultConfig(cfg)) {
            cfg = ConfigurationAPI.getEvervaultConfiguration()
        }
        applyEvervaultConfiguration(cfg)
    }
}
