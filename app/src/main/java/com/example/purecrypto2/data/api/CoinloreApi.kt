package com.example.purecrypto2.data.api

import com.example.purecrypto2.data.model.CryptoResponse
import retrofit2.http.GET

interface CoinloreApi {
    @GET("api/tickers/")
    suspend fun getCryptos(): CryptoResponse
} 