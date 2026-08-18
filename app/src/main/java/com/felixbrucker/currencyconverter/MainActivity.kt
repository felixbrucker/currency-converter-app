package com.felixbrucker.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.felixbrucker.currencyconverter.ui.ConversionScreen
import com.felixbrucker.currencyconverter.ui.ConversionViewModel
import com.felixbrucker.currencyconverter.ui.SettingsScreen
import com.felixbrucker.currencyconverter.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ConversionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = "conversion"
                ) {
                    composable("conversion") {
                        ConversionScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable("settings") {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

