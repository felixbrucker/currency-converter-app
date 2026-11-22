package com.felixbrucker.currencyconverter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
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
                val slideInFromRight = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 6 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))

                val slideOutToLeft = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth / 6 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))

                val slideInFromLeft = slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 6 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing))

                val slideOutToRight = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth / 6 },
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))


                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "conversion",
                    predictivePopEnterTransition = {
                        val isSlideScreen = initialState.destination.route?.startsWith("settings") == true
                        if (isSlideScreen) slideInFromLeft else EnterTransition.None
                    },
                    predictivePopExitTransition = {
                        val isSlideScreen = initialState.destination.route?.startsWith("settings") == true
                        if (isSlideScreen) slideOutToRight else ExitTransition.None
                    },
                ) {
                    composable(
                        route = "conversion",
                        exitTransition = { slideOutToLeft },
                        popEnterTransition = { slideInFromLeft },
                    ) {
                        ConversionScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(
                        route = "settings",
                        enterTransition = { slideInFromRight },
                        exitTransition = { slideOutToRight },
                    ) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = {
                                navController.popBackStackSafely()
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun NavController.popBackStackSafely(): Boolean {
    val currentEntry = currentBackStackEntry ?: return false
    if (currentEntry.lifecycle.currentState != Lifecycle.State.RESUMED) {
        return false
    }
    if (previousBackStackEntry == null) {
        return false
    }
    return popBackStack()
}
