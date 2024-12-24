package com.iste.paymentx.data.model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApi {
    @POST("/api/users/login")
    suspend fun login(
        @Header("Authorization") authToken: String,
        @Body user: User // Body parameter for the new todo item
    ): Response<response>

    @PATCH("/api/users/attach-id")
    suspend fun attachId(
        @Header("Authorization") authToken: String,
        @Body idCardUid: String
    ): Response<response>
}

