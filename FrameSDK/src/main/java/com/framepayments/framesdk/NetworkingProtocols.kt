package com.framepayments.framesdk
import okhttp3.Request
import okhttp3.Response

interface URLSessionProtocol {
    suspend fun execute(request: Request): Response
}

data class QueryItem(val name: String, val value: String?)
interface FrameNetworkingEndpoints {
    val endpointURL: String
    val httpMethod: String
    val queryItems: List<QueryItem>?
}

sealed class NetworkingError : Exception() {
    data object InvalidURL : NetworkingError() {
        private fun readResolve(): Any = InvalidURL
    }
    data object DecodingFailed : NetworkingError() {
        private fun readResolve(): Any = DecodingFailed
    }
    data class ServerError(val statusCode: Int) : NetworkingError()
    data object UnknownError : NetworkingError() {
        private fun readResolve(): Any = UnknownError
    }
}