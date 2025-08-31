package com.example.purecrypto2.data.model

import com.google.gson.annotations.SerializedName

data class CryptoResponse(
    @SerializedName("data") val data: List<Crypto>,
    @SerializedName("info") val info: Info
)

data class Info(
    @SerializedName("coins_num") val coins_num: Int,
    @SerializedName("time") val time: Long
)
