package com.iste.paymentx.data.model

data class TransactionRequest(
    val idCardUID : String,
    val amount: Int,
    val pin: String
)
