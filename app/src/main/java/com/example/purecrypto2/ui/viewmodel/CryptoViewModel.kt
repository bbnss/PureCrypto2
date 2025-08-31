package com.example.purecrypto2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.purecrypto2.data.model.Crypto
import com.example.purecrypto2.data.repository.CryptoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class CryptoUiState {
    data object Loading : CryptoUiState()
    data class Success(val cryptos: List<Crypto>) : CryptoUiState()
    data class Error(val message: String) : CryptoUiState()
}

sealed class BannerState {
    data object Loading : BannerState()
    data class Success(val cryptos: List<Crypto>) : BannerState()
    data class Error(val message: String) : BannerState()
}

enum class SortType {
    MARKET_CAP, CHANGE_1H, CHANGE_24H, CHANGE_7D
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

class CryptoViewModel : ViewModel() {
    private val repository = CryptoRepository()
    private val _uiState = MutableStateFlow<CryptoUiState>(CryptoUiState.Loading)
    val uiState: StateFlow<CryptoUiState> = _uiState.asStateFlow()

    private val _bannerState = MutableStateFlow<BannerState>(BannerState.Loading)
    val bannerState: StateFlow<BannerState> = _bannerState.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _lastUpdatedTimeString = MutableStateFlow<String?>(null)
    val lastUpdatedTimeString: StateFlow<String?> = _lastUpdatedTimeString.asStateFlow()

    private val _currentSortType = MutableStateFlow(SortType.MARKET_CAP)
    val currentSortType: StateFlow<SortType> = _currentSortType.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DESCENDING) // Default to descending for market cap
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    init {
        loadCryptos()
    }

    /**
     * Carica i dati delle criptovalute dall'API
     */
    private fun loadCryptos(fromRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!fromRefresh) { // Mostra Loading solo per il caricamento iniziale
                _uiState.value = CryptoUiState.Loading
                _bannerState.value = BannerState.Loading // Anche il banner può avere uno stato di caricamento iniziale
            }
            _isRefreshing.value = true

            try {
                val cryptoData = repository.getCryptos()
                var cryptos = cryptoData.cryptos
                val lastUpdateTime = cryptoData.lastUpdateTime

                // Convert Unix timestamp to human-readable date
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val date = Date(lastUpdateTime * 1000L) // Convert seconds to milliseconds
                _lastUpdatedTimeString.value = sdf.format(date)

                // Apply sorting
                cryptos = applySorting(cryptos, _currentSortType.value, _sortOrder.value)

                if (cryptos.isEmpty()) {
                    _uiState.value = CryptoUiState.Error("Nessun dato disponibile")
                    _bannerState.value = BannerState.Error("Nessun dato disponibile")
                } else {
                    _uiState.value = CryptoUiState.Success(cryptos)
                    _bannerState.value = BannerState.Success(cryptos)
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Errore sconosciuto"
                _uiState.value = CryptoUiState.Error(errorMessage)
                _bannerState.value = BannerState.Error(errorMessage)
                _lastUpdatedTimeString.value = null // Clear last updated time on error
            } finally {
                _isRefreshing.value = false // Assicura che isRefreshing sia impostato a false alla fine
            }
        }
    }

    private fun applySorting(cryptos: List<Crypto>, sortType: SortType, sortOrder: SortOrder): List<Crypto> {
        val sortedList = when (sortType) {
            SortType.MARKET_CAP -> cryptos.sortedWith(compareByDescending<Crypto> { it.market_cap?.toDoubleOrNull() ?: 0.0 })
            SortType.CHANGE_1H -> cryptos.sortedWith(compareByDescending<Crypto> { it.price_change_percentage_1h?.toDoubleOrNull() ?: 0.0 })
            SortType.CHANGE_24H -> cryptos.sortedWith(compareByDescending<Crypto> { it.price_change_percentage_24h?.toDoubleOrNull() ?: 0.0 })
            SortType.CHANGE_7D -> cryptos.sortedWith(compareByDescending<Crypto> { it.price_change_percentage_7d?.toDoubleOrNull() ?: 0.0 })
        }
        return if (sortOrder == SortOrder.ASCENDING) sortedList.reversed() else sortedList
    }
    
    /**
     * Funzione pubblica per ricaricare i dati
     */
    fun refreshData() {
        loadCryptos(fromRefresh = true)
    }

    fun setSort(sortType: SortType) {
        viewModelScope.launch {
            if (_currentSortType.value == sortType) {
                // Toggle sort order if the same button is clicked again
                _sortOrder.value = if (_sortOrder.value == SortOrder.ASCENDING) SortOrder.DESCENDING else SortOrder.ASCENDING
            } else {
                _currentSortType.value = sortType
                _sortOrder.value = SortOrder.DESCENDING // Default to descending for new sort type
            }
            loadCryptos(fromRefresh = true) // Reload with new sorting
        }
    }
}
