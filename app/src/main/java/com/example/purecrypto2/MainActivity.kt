package com.example.purecrypto2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.purecrypto2.ui.screen.CryptoListScreen
import com.example.purecrypto2.ui.theme.CryptoTerminalTheme
import com.example.purecrypto2.ui.viewmodel.CryptoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoTerminalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CryptoListScreen(viewModel = CryptoViewModel())
                }
            }
        }
    }
}
