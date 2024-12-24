package com.iste.paymentx.data.model

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: UserApi by lazy {
        createRetrofit()
    }
    private fun createRetrofit(): UserApi {
        return Retrofit.Builder()
            .baseUrl("https://paymentx-backend.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }
}