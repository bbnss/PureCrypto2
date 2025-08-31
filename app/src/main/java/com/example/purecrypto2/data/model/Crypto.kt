package com.example.purecrypto2.data.model

import com.google.gson.annotations.SerializedName

data class Crypto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("rank") val rank: Int,
    @SerializedName("price_usd") val current_price: String,
    @SerializedName("percent_change_1h") val price_change_percentage_1h: String,
    @SerializedName("percent_change_24h") val price_change_percentage_24h: String,
    @SerializedName("percent_change_7d") val price_change_percentage_7d: String,
    @SerializedName("market_cap_usd") val market_cap: String,
    @SerializedName("volume24") val volume24: String,
    @SerializedName("csupply") val circulatingSupply: String?,
    @SerializedName("tsupply") val totalSupply: String?,
    @SerializedName("msupply") val maxSupply: String?
) {
    companion object {
        val coingeckoIcons = mapOf(
            "BTC" to Pair(1, "bitcoin"),
            "ETH" to Pair(279, "ethereum"),
            "USDT" to Pair(325, "tether"),
            "BNB" to Pair(825, "binance-coin-logo"),
            "USDC" to Pair(6319, "usdc"),
            "XRP" to Pair(44, "xrp-symbol-white-128"),
            "ADA" to Pair(975, "cardano"),
            "SOL" to Pair(4128, "solana"),
            "DOGE" to Pair(5, "dogecoin"),
            "MATIC" to Pair(4713, "matic-token-icon")
            // Estendibile con altre crypto
        )
    }

    val imageUrl: String
        get() = coingeckoIcons[symbol.uppercase()]?.let { (id, name) ->
            "https://assets.coingecko.com/coins/images/$id/standard/$name.png"
        } ?: "https://assets.coingecko.com/coins/images/1/standard/bitcoin.png" // Default: bitcoin
}
