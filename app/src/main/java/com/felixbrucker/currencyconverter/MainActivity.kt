package com.felixbrucker.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.felixbrucker.currencyconverter.ui.ConversionScreen
import com.felixbrucker.currencyconverter.ui.ConversionViewModel
import com.felixbrucker.currencyconverter.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ConversionScreen(viewModel = viewModel)
            }
        }
    }
}

