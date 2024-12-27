package com.iste.paymentx.data.model

import androidx.core.app.NotificationCompat.MessagingStyle.Message

data class response(
    val success : Boolean = true,
    val message: String? = null,
    val user: User? = null,
    val wallet: Wallet? = null
)
