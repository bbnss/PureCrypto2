package com.example.purecrypto2.data.repository

import android.content.Context
import org.json.JSONObject
import java.io.InputStream

object ThumbnailProvider {
    /**
     * Restituisce il path relativo in assets dell'icona per il simbolo dato.
     * Esempio: "downloaded_icons/BTC.png"
     */
    fun getLocalAssetPath(symbol: String): String {
        return "downloaded_icons/${symbol.uppercase()}.png"
    }
}
