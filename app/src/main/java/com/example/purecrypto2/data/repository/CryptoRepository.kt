package com.example.purecrypto2.data.repository

import android.util.Log
import com.example.purecrypto2.data.api.CoinloreApi
import com.example.purecrypto2.data.model.Crypto
import com.example.purecrypto2.data.model.CryptoData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class CryptoRepository {
    private val api: CoinloreApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.coinlore.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(CoinloreApi::class.java)
    }

    suspend fun getCryptos(): CryptoData {
        return try {
            val response = api.getCryptos()
            CryptoData(cryptos = response.data, lastUpdateTime = response.info.time)
        } catch (e: Exception) {
            Log.e("CryptoRepository", "Error fetching cryptos: ${e.message}", e)
            CryptoData(cryptos = emptyList(), lastUpdateTime = 0L) // Return default or handle error appropriately
        }
    }
}
