package com.iste.paymentX.data.model

data class TransactionRequest(
    val idCardUID : String,
    val amount: Int,
    val pin: String
)
