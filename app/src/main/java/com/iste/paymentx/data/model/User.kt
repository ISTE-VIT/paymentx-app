package com.iste.paymentx.data.model

data class User(
    val email: String?,
    val displayName: String?,
    val uid: String,
    val isMerchant: Boolean, // Indicates if the user is a merchant, required
    val phoneNumber: String? = null, // Nullable phone number with default value as null
    val idCardUID: String? = null,
    val pin:String? = null
)

