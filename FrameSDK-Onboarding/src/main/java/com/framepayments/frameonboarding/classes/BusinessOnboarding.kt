package com.framepayments.frameonboarding.classes

/** Role a person holds on a business account. The owners API requires one. */
internal enum class BusinessRole(val apiValue: String, val label: String) {
    OWNER("owner", "Owner (I own 25% or more of this business)"),
    CONTROLLER("controller", "Controller")
}

/** Legal structure written to `profile.business.business_type`. */
internal enum class BusinessStructure(val apiValue: String, val label: String) {
    SOLE_PROPRIETORSHIP("sole_proprietorship", "Sole Proprietorship"),
    SINGLE_MEMBER_LLC("single_member_llc", "Single Member LLC"),
    MULTI_MEMBER_LLC("multi_member_llc", "Multi-member LLC"),
    PRIVATE_CORPORATION("private_corporation", "Private Corporation"),
    PUBLIC_CORPORATION("public_corporation", "Public Corporation"),
    PRIVATE_PARTNERSHIP("private_partnership", "Private Partnership"),
    NON_PROFIT("non_profit", "Non Profit")
}
