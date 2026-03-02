package com.framepayments.frameonboarding.prove

/**
 * User information returned after successful Prove authentication and backend verify.
 * Mirrors the iOS contract so a shared backend can serve both platforms.
 */
data class ProveUserInfo(
    val firstName: String,
    val lastName: String
)
