package com.felixbrucker.currencyconverter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.felixbrucker.currencyconverter.model.ConversionRowState
import com.felixbrucker.currencyconverter.model.CurrencyType
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun ConversionCard(
    rowState: ConversionRowState,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    onRowFocus: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onFinishInput: () -> Unit = {},
    onCurrencyClick: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isFieldFocused by remember { mutableStateOf(false) }

    val isActive = rowState.isFocused

    // Track if we've already performed the initial focus for this row while it's active.
    // Using rememberSaveable ensures this persists across navigation/process death,
    // preventing auto-focus when returning to the screen.
    var hasRequestedInitialFocus by rememberSaveable(rowState.currency.code) { mutableStateOf(false) }

    // Automatically request focus into the text field on the first tap when this row becomes active
    LaunchedEffect(isActive) {
        if (isActive && !hasRequestedInitialFocus) {
            delay(50.milliseconds)
            try {
                focusRequester.requestFocus()
                hasRequestedInitialFocus = true
            } catch (_: Exception) {}
        } else if (!isActive) {
            // Reset when the row is no longer active so it can be focused again if re-selected later
            hasRequestedInitialFocus = false
        }
    }

    val cardBorder = when {
        isDragging -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        isActive -> BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val cardBg = when {
        isDragging -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isActive -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("conversion_card_${rowState.currency.code}")
            .clickable {
                if (!isActive) {
                    onRowFocus(rowState.currency.code)
                } else {
                    try {
                        focusRequester.requestFocus()
                    } catch (_: Exception) {}
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 8.dp else if (isActive) 1.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Currency Flag & Code / Name (Left)
            Surface(
                onClick = { onCurrencyClick(rowState.currency.code) },
                shape = RoundedCornerShape(14.dp),
                color = Color.Transparent,
                modifier = Modifier.testTag("currency_badge_${rowState.currency.code}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // Flag avatar
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive || isDragging) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.background
                            )
                            .border(
                                1.dp,
                                if (isActive || isDragging) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                CircleShape
                            )
                    ) {
                        when (rowState.currency.type) {
                            is CurrencyType.Fiat -> Text(
                                text = rowState.currency.type.flagEmoji,
                                fontSize = 22.sp
                            )
                            is CurrencyType.Crypto -> AsyncImage(
                                model = rowState.currency.type.imageUrl,
                                contentDescription = "${rowState.currency.name} Icon",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rowState.currency.code,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (rowState.isStale || rowState.isRateUnavailable) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IndicatorBox(
                                    text = if (rowState.isRateUnavailable) "RATE UNAVAILABLE" else "STALE RATE",
                                    backgroundColor = if (rowState.isRateUnavailable) MaterialTheme.colorScheme.errorContainer else Color(0xFFFBC02D).copy(alpha = 0.1f),
                                    textColor = if (rowState.isRateUnavailable) MaterialTheme.colorScheme.error else Color(0xFFFBC02D),
                                    iconImageVector = Icons.Default.Warning,
                                    iconTint = if (rowState.isRateUnavailable) MaterialTheme.colorScheme.error else Color(0xFFFBC02D),
                                )
                            }
                        }
                        Text(
                            text = rowState.currency.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Amount Field and Exchange Rate below it (Right)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (!isActive) {
                            onRowFocus(rowState.currency.code)
                        } else {
                            try {
                                focusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                    }
            ) {
                // Top: Symbol and Amount grouped together at the right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Currency Symbol
                    Text(
                        text = rowState.currency.symbol,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    if (isActive) {
                        BasicTextField(
                            value = rowState.enteredText,
                            onValueChange = { onAmountChange(it) },
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    val wasFocused = isFieldFocused
                                    isFieldFocused = focusState.isFocused
                                    if (wasFocused && !focusState.isFocused) {
                                        onFinishInput()
                                    }
                                }
                                .testTag("amount_input_${rowState.currency.code}"),
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    onFinishInput()
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    contentAlignment = Alignment.CenterEnd,
                                    modifier = Modifier.width(IntrinsicSize.Min),
                                ) {
                                    if (rowState.enteredText.isEmpty()) {
                                        Text(
                                            text = if (isFieldFocused) rowState.hintAmountText else rowState.displayedAmountText,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 24.sp,
                                                textAlign = TextAlign.End,
                                                color = if (isFieldFocused) {
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(
                            text = rowState.displayedAmountText,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.End,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("amount_display_${rowState.currency.code}")
                        )
                    }
                }

                // Bottom: Exchange rate positioned on the right below the amount
                if (!isActive && rowState.baseExchangeRateText.isNotBlank()) {
                    Text(
                        text = rowState.baseExchangeRateText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = when {
                                rowState.isRateUnavailable -> MaterialTheme.colorScheme.error
                                rowState.isStale -> Color(0xFFFBC02D)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .testTag("rate_text_${rowState.currency.code}")
                    )
                }
            }
        }
    }
}
