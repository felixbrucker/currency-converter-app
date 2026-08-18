package com.felixbrucker.currencyconverter

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.felixbrucker.currencyconverter.model.ConversionRowState
import com.felixbrucker.currencyconverter.model.Currency
import com.felixbrucker.currencyconverter.ui.components.ConversionCard
import com.felixbrucker.currencyconverter.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleRow = ConversionRowState(
      currency = Currency("NZD", "New Zealand Dollar", "$", "🇳🇿", false, "New Zealand", 2, true),
      isFocused = true,
      enteredText = "1.00",
      displayedAmountText = "1.00",
      hintAmountText = "1.00",
      isHintActive = false,
      baseExchangeRateText = "",
      displayOrder = 0
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          ConversionCard(
            rowState = sampleRow,
            isFirst = true,
            isLast = false,
            onRowFocus = {},
            onAmountChange = {},
            onCurrencyClick = {},
            onMoveUp = {},
            onMoveDown = {},
            onRemove = {},
            onSetBase = {}
          )
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

