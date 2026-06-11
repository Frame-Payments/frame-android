package com.framepayments.framesdk_ui

/** Controls whether billing address fields are shown and required in [FrameCheckoutView]. */
enum class AddressMode {
    /** Address fields are shown and the customer must complete them before paying. */
    REQUIRED,

    /** Address fields are shown but the customer may leave them blank. */
    OPTIONAL,

    /** Address fields are hidden entirely. */
    HIDDEN
}
