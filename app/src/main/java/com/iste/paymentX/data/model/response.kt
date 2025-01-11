package com.iste.paymentX.data.model

data class response(
    val success : Boolean = true,
    val message: String? = null,
    val user: User? = null,
    val wallet: Wallet? = null
)
