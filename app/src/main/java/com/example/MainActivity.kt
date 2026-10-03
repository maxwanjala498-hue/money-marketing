package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.MainScreen
import com.example.ui.theme.BinanceConnectTheme
import com.example.ui.viewmodel.BinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BinanceConnectTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
