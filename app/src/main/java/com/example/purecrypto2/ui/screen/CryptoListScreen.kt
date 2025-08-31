package com.example.purecrypto2.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Refresh
import coil.compose.AsyncImage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.purecrypto2.R
import com.example.purecrypto2.data.model.Crypto
import com.example.purecrypto2.data.repository.ThumbnailProvider
import com.example.purecrypto2.ui.theme.CryptoTerminalTheme
import com.example.purecrypto2.ui.theme.LedGlowColor
import com.example.purecrypto2.ui.theme.LedTextColor
import com.example.purecrypto2.ui.theme.LedTextStyle
import com.example.purecrypto2.ui.viewmodel.BannerState
import com.example.purecrypto2.ui.viewmodel.CryptoUiState
import com.example.purecrypto2.ui.viewmodel.CryptoViewModel
import com.example.purecrypto2.ui.viewmodel.SortOrder
import com.example.purecrypto2.ui.viewmodel.SortType
import kotlinx.coroutines.delay

private fun getPriceColor(priceChange: String?): Color {
    return try {
        if (priceChange.isNullOrBlank()) {
            Color(0xFF00FF00)
        } else {
            val trimmedValue = priceChange.trim()
            val value = trimmedValue.toDouble()
            when {
                value >= 0.0 -> Color(0xFF00FF00)
                else -> Color(0xFFFF0000)
            }
        }
    } catch (e: NumberFormatException) {
        android.util.Log.w("CryptoListScreen", "Errore nella conversione del valore: $priceChange", e)
        Color(0xFF00FF00)
    }
}

@Composable
fun CryptoListScreen(viewModel: CryptoViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val bannerState by viewModel.bannerState.collectAsState()
    val lastUpdatedTimeString by viewModel.lastUpdatedTimeString.collectAsState()
    val currentSortType by viewModel.currentSortType.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var selectedCrypto by remember { mutableStateOf<Crypto?>(null) }
    var currentBannerIndex by remember { mutableStateOf(0) }
    var currentBannerText by remember { mutableStateOf("CRYPTO TERMINAL") }
    var showVolume by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        while (true) {
            delay(120000)
            viewModel.refreshData()
        }
    }

    val bannerCryptos = remember(bannerState) {
        val currentBannerStateValue = bannerState
        if (currentBannerStateValue is BannerState.Success) {
            currentBannerStateValue.cryptos
        } else {
            null
        }
    }
    LaunchedEffect(bannerCryptos) {
        if (bannerCryptos != null) {
            while (true) {
                if (bannerCryptos.isNotEmpty()) {
                    try {
                        val crypto = bannerCryptos[currentBannerIndex]

                        val marketCap = try {
                            val valueStr = crypto.market_cap.trim()
                            if (valueStr.isEmpty()) throw NumberFormatException("Empty market cap")
                            val value = valueStr.toDouble()
                            when {
                                value >= 1_000_000_000_000 -> "%.2f".format(value / 1_000_000_000_000) + "T"
                                value >= 1_000_000_000 -> "%.2f".format(value / 1_000_000_000) + "B"
                                value >= 1_000_000 -> "%.2f".format(value / 1_000_000) + "M"
                                else -> "%.2f".format(value)
                            }
                        } catch (e: NumberFormatException) {
                            "N/A"
                        }

                        val volume24h = try {
                            val valueStr = crypto.volume24.trim()
                            if (valueStr.isEmpty()) throw NumberFormatException("Empty volume")
                            val value = valueStr.toDouble()
                            when {
                                value >= 1_000_000_000_000 -> "%.2f".format(value / 1_000_000_000_000) + "T"
                                value >= 1_000_000_000 -> "%.2f".format(value / 1_000_000_000) + "B"
                                value >= 1_000_000 -> "%.2f".format(value / 1_000_000) + "M"
                                else -> "%.2f".format(value)
                            }
                        } catch (e: NumberFormatException) {
                            "N/A"
                        }

                        currentBannerText = if (showVolume) {
                            "${crypto.symbol} VOL $${volume24h}"
                        } else {
                            "${crypto.symbol} CAP $${marketCap}"
                        }

                        delay(2000)

                        showVolume = !showVolume

                        if (!showVolume) {
                            currentBannerIndex = (currentBannerIndex + 1) % bannerCryptos.size
                        }
                    } catch (e: Exception) {
                        currentBannerText = "CRYPTO TERMINAL"
                        delay(2000)
                    }
                } else {
                    delay(2000)
                }
            }
        } else {
            delay(2000)
        }
    }

    CryptoTerminalTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color.Black)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val bannerCrypto = when (val currentBannerState = bannerState) {
                            is BannerState.Success -> currentBannerState.cryptos.getOrNull(currentBannerIndex)
                            else -> null
                        }
                        if (bannerCrypto != null) {
                            val assetPath = "file:///android_asset/" + com.example.purecrypto2.data.repository.ThumbnailProvider.getLocalAssetPath(bannerCrypto.symbol)
                            val context = LocalContext.current
                            val assetExists = try {
                                context.assets.open("downloaded_icons/${bannerCrypto.symbol.uppercase()}.png").close()
                                true
                            } catch (e: Exception) {
                                false
                            }
                            if (assetExists) {
                                AsyncImage(
                                    model = assetPath,
                                    contentDescription = bannerCrypto.name,
                                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                                    error = painterResource(id = R.drawable.ic_placeholder),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Color(0xFF44475a)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bannerCrypto.symbol.uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Text(
                            text = currentBannerText,
                            style = LedTextStyle,
                            color = LedTextColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.drawBehind {
                                drawTextGlow()
                            }
                        )
                    }
                }

                // New row for CoinLore last update and sorting buttons
                lastUpdatedTimeString?.let { timeString ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CoinLore $timeString", // Shortened text
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // CAP Button
                            Button(
                                onClick = { viewModel.setSort(SortType.MARKET_CAP) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSortType == SortType.MARKET_CAP) Color(0xFF6200EE) else Color(0xFF44475a)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("CAP", fontSize = 10.sp, color = Color.White)
                                if (currentSortType == SortType.MARKET_CAP) {
                                    Icon(
                                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // 1H Button
                            Button(
                                onClick = { viewModel.setSort(SortType.CHANGE_1H) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSortType == SortType.CHANGE_1H) Color(0xFF6200EE) else Color(0xFF44475a)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("1H", fontSize = 10.sp, color = Color.White)
                                if (currentSortType == SortType.CHANGE_1H) {
                                    Icon(
                                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // 24H Button
                            Button(
                                onClick = { viewModel.setSort(SortType.CHANGE_24H) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSortType == SortType.CHANGE_24H) Color(0xFF6200EE) else Color(0xFF44475a)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("24H", fontSize = 10.sp, color = Color.White)
                                if (currentSortType == SortType.CHANGE_24H) {
                                    Icon(
                                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            // 7D Button
                            Button(
                                onClick = { viewModel.setSort(SortType.CHANGE_7D) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentSortType == SortType.CHANGE_7D) Color(0xFF6200EE) else Color(0xFF44475a)
                                ),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("7D", fontSize = 10.sp, color = Color.White)
                                if (currentSortType == SortType.CHANGE_7D) {
                                    Icon(
                                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                when (uiState) {
                    is CryptoUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CryptoUiState.Success -> {
                        val cryptos = (uiState as? CryptoUiState.Success)?.cryptos ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(cryptos) { crypto ->
                                CryptoItem(
                                    crypto = crypto,
                                    onItemClick = { selectedCrypto = crypto },
                                    currentSortType = currentSortType // Pass currentSortType
                                )
                            }
                        }
                    }
                    is CryptoUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (uiState as CryptoUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        selectedCrypto?.let { crypto ->
            CryptoDetailDialog(
                crypto = crypto,
                onDismiss = { selectedCrypto = null }
            )
        }
    }
}

@Composable
private fun CryptoItem(
    crypto: Crypto,
    onItemClick: (Crypto) -> Unit,
    currentSortType: SortType // Added currentSortType parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onItemClick(crypto) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val assetPath = "file:///android_asset/" + com.example.purecrypto2.data.repository.ThumbnailProvider.getLocalAssetPath(crypto.symbol)
            val context = LocalContext.current
            val assetExists = try {
                context.assets.open("downloaded_icons/${crypto.symbol.uppercase()}.png").close()
                true
            } catch (e: Exception) {
                false
            }
            if (assetExists) {
                AsyncImage(
                    model = assetPath,
                    contentDescription = crypto.name,
                    placeholder = painterResource(id = R.drawable.ic_placeholder),
                    error = painterResource(id = R.drawable.ic_placeholder),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            } else {
                // Placeholder dinamico con simbolo
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color(0xFF44475a)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = crypto.symbol.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = crypto.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = crypto.symbol.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1.2f)
            ) {
                Text(
                    text = "$${crypto.current_price}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = getPriceColor(crypto.price_change_percentage_24h),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "1h:${crypto.price_change_percentage_1h}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = getPriceColor(crypto.price_change_percentage_1h),
                        maxLines = 1
                    )
                    val displayedChange = if (currentSortType == SortType.CHANGE_7D) {
                        crypto.price_change_percentage_7d
                    } else {
                        crypto.price_change_percentage_24h
                    }
                    val changeLabel = if (currentSortType == SortType.CHANGE_7D) "7d" else "24h"

                    Text(
                        text = "$changeLabel:${
                            if (displayedChange.isNullOrBlank())
                                "N/A"
                            else
                                "$displayedChange%"
                        }",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = getPriceColor(displayedChange),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTextGlow() {
    drawCircle(
        color = LedGlowColor,
        radius = 100f,
        center = center
    )
}
