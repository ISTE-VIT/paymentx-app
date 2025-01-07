package com.iste.paymentx.data.model

data class Transaction(
    val userId: String,
    val merchantId: String,
    val userName: String,
    val merchantName: String,
    val idCardUID: String,
    val amount: Int,
    val timestamp: String, // Store timestamp as a String in ISO 8601 format
    val status: String
)
