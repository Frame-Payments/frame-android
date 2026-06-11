package com.framepayments.framesdk

/** Base URL constants used by [FrameNetworking] when constructing API requests. */
class NetworkingConstants {
    /** Holds global URL constants for the Frame SDK. */
    companion object {
        /** The default Frame API base URL. Override via [FrameNetworking.mainApiUrl] for testing or staging. */
        var MAIN_API_URL: String = "https://api.framepayments.com"
    }
}