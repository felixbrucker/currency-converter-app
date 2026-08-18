package com.felixbrucker.currencyconverter.model

data class ConversionRowState(
    val currency: Currency,
    val isFocused: Boolean,
    val enteredText: String, // what the user has currently typed (empty if untouched or cleared)
    val displayedAmountText: String, // formatted value shown in the field
    val hintAmountText: String, // placeholder when focused and cleared
    val isHintActive: Boolean, // whether the input is currently showing the hint (cleared on focus)
    val baseExchangeRateText: String, // e.g. "1 NZD = 0.5922 USD"
    val displayOrder: Int
)
