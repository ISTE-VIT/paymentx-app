package com.iste.paymentX.data.model

data class Wallet(
    val userId: String,
    val userName: String,
    val balance: Int = 0,
    val cardUID: String? = null,
    val isMerchant: Boolean
)
