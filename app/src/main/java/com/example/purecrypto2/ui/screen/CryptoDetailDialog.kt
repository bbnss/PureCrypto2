package com.example.purecrypto2.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource // Import stringResource
import coil.compose.AsyncImage
import com.example.purecrypto2.R // Import R for string resources
import com.example.purecrypto2.data.model.Crypto
import com.example.purecrypto2.data.repository.ThumbnailProvider

@Composable
fun CryptoDetailDialog(
    crypto: Crypto,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            val context = LocalContext.current
            val assetExists = try {
                context.assets.open("downloaded_icons/${crypto.symbol.uppercase()}.png").close()
                true
            } catch (e: Exception) {
                false
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 380.dp)
            ) {
                // Sfondo: immagine se esiste, altrimenti placeholder dinamico
                if (assetExists) {
                    val assetPath = "file:///android_asset/" + ThumbnailProvider.getLocalAssetPath(crypto.symbol)
                    AsyncImage(
                        model = assetPath,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .alpha(0.38f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFF44475a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = crypto.symbol.uppercase(),
                            color = Color.White.copy(alpha = 0.18f),
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                }
                // Overlay contenuto
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .padding(vertical = 18.dp, horizontal = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = crypto.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    ModernDetail(label = stringResource(R.string.crypto_symbol), value = crypto.symbol)
                    ModernDetail(label = stringResource(R.string.crypto_rank), value = "#${crypto.rank}")
                    ModernDetail(label = stringResource(R.string.crypto_price), value = "$${formatCryptoValue(crypto.current_price, stringResource(R.string.not_available))}")
                    ModernDetail(label = stringResource(R.string.crypto_market_cap), value = "$${formatCryptoValue(crypto.market_cap, stringResource(R.string.not_available))}")
                    ModernDetail(label = stringResource(R.string.crypto_volume_24h), value = "$${formatCryptoValue(crypto.volume24, stringResource(R.string.not_available))}")
                    ModernDetail(label = stringResource(R.string.crypto_circulating_supply), value = crypto.circulatingSupply ?: stringResource(R.string.not_available))
                    ModernDetail(label = stringResource(R.string.crypto_total_supply), value = crypto.totalSupply ?: stringResource(R.string.not_available))
                    ModernDetail(label = stringResource(R.string.crypto_max_supply), value = crypto.maxSupply ?: stringResource(R.string.not_available))
                    ModernDetail(label = stringResource(R.string.crypto_change_1h), value = "${crypto.price_change_percentage_1h}%")
                    ModernDetail(label = stringResource(R.string.crypto_change_24h), value = "${crypto.price_change_percentage_24h}%")
                    ModernDetail(label = stringResource(R.string.crypto_change_7d), value = "${crypto.price_change_percentage_7d}%")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close_button))
            }
        }
    )
}

@Composable
private fun ModernDetail(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFFB0B0B0),
            modifier = Modifier.padding(start = 2.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White.copy(alpha = 0.07f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(vertical = 4.dp, horizontal = 8.dp)
        )
    }
}

// Formattazione intelligente per valori crypto
fun formatCryptoValue(raw: String?, notAvailableString: String): String {
    if (raw == null) return notAvailableString
    val value = raw.replace(",", "").toDoubleOrNull() ?: return raw
    return if (value >= 1) {
        // max 2 decimali
        "%,.2f".format(value)
    } else {
        // almeno 3 cifre significative diverse da zero dopo la virgola
        // es: 0,0000432
        val s = "%f".format(value)
        val afterDot = s.substringAfter('.')
        // Trova la posizione della terza cifra diversa da zero
        var count = 0
        var idx = 0
        for (c in afterDot) {
            idx++
            if (c != '0') count++
            if (count == 3) break
        }
        val totalDigits = idx
        val pattern = "%.${totalDigits}f"
        pattern.format(value).trimEnd('0').trimEnd('.')
    }
}
