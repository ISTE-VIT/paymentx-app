package com.iste.paymentx.data.model

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
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
        @Body idCardUID: AttachIdRequest
    ): Response<response>

    @PATCH("/api/users/attach-phone")
    suspend fun attachPhone(
        @Header("Authorization") authToken: String,
        @Body phoneNumber: AttachPhoneRequest
    ): Response<response>

    @PATCH("/api/users/create-pin")
    suspend fun createPin(
        @Header("Authorization") authToken: String,
        @Body pin: CreatePinRequest
    ): Response<response>

    @GET("/")
    suspend fun init(
    ): Response<response>

    @GET("/api/users/check-user")
    suspend fun checkUser(
        @Header("Authorization") authToken: String
    ): Response<response>

    @POST("/api/users/verify-pin")
    suspend fun verifyPin(
        @Header("Authorization") authToken: String,
        @Body pin: CreatePinRequest
    ): Response<response>

    @GET("/api/wallets/")
    suspend fun getWallet(
        @Header("Authorization") authToken: String
    ): Response<response>

    @PATCH("/api/wallets/topup")
    suspend fun topup(
        @Header("Authorization") authToken: String,
        @Body details: WalletRequest
    ): Response<response>

    @PATCH("/api/wallets/withdraw")
    suspend fun withdraw(
        @Header("Authorization") authToken: String,
        @Body details: WalletRequest
    ): Response<response>
}

