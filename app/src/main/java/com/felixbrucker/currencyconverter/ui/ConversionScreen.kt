package com.felixbrucker.currencyconverter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felixbrucker.currencyconverter.ui.components.ConversionCard
import com.felixbrucker.currencyconverter.ui.components.CurrencySelectorSheet
import com.felixbrucker.currencyconverter.ui.components.RatesInfoDialog
import com.felixbrucker.currencyconverter.ui.components.UpdateTimerHeader
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionScreen(
    viewModel: ConversionViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allCurrenciesWithSelection by viewModel.allCurrenciesWithSelection.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()

    var showCurrencySheet by remember { mutableStateOf(false) }
    var showRatesInfoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ConversionViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message, duration = event.duration)
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val listState = rememberLazyListState()

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Live countdown & last updated header
            UpdateTimerHeader(
                countdownSeconds = countdownSeconds,
                maxCountdownSeconds = uiState.maxCountdownSeconds,
                lastUpdatedTimestamp = uiState.lastUpdatedTimestamp,
                isOnline = uiState.isOnline,
                isRefreshing = uiState.isRefreshing,
                onSearchClick = { showCurrencySheet = true },
                onSettingsClick = onNavigateToSettings
            )

            // Conversion list with pull to refresh
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refreshRates() },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(
                        items = uiState.rows,
                        key = { _, row -> row.currency.code }
                    ) { index, rowState ->
                        val isDragging = draggedIndex == index
                        val currentItemIndex by rememberUpdatedState(index)

                        Box(
                            modifier = Modifier
                                .animateItem()
                                .zIndex(if (isDragging) 10f else 1f)
                                .graphicsLayer {
                                    translationY = if (isDragging) dragOffsetY else 0f
                                    scaleX = if (isDragging) 1.03f else 1f
                                    scaleY = if (isDragging) 1.03f else 1f
                                }
                                .pointerInput(uiState.rows.size, rowState.currency.code) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedIndex = currentItemIndex
                                            dragOffsetY = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffsetY += dragAmount.y

                                            val currentIndex = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                            val visibleItems = listState.layoutInfo.visibleItemsInfo
                                            val currentItem = visibleItems.firstOrNull { it.index == currentIndex } ?: return@detectDragGesturesAfterLongPress
                                            val currentCenterY = currentItem.offset + currentItem.size / 2f + dragOffsetY

                                            for (item in visibleItems) {
                                                if (item.index != currentIndex && item.index in uiState.rows.indices) {
                                                    val itemTop = item.offset.toFloat()
                                                    val itemBottom = (item.offset + item.size).toFloat()
                                                    if (currentCenterY in itemTop..itemBottom) {
                                                        val targetIndex = item.index
                                                        viewModel.onReorder(currentIndex, targetIndex)
                                                        dragOffsetY += (currentItem.offset - item.offset)
                                                        draggedIndex = targetIndex
                                                        break
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            draggedIndex = null
                                            dragOffsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                            dragOffsetY = 0f
                                        }
                                    )
                                }
                        ) {
                            val currentCode = rowState.currency.code
                            // Use remember(currentCode) and manual construction to avoid rememberSaveable
                            // which restores the "dismissed" state for the item's key upon re-addition.
                            val threshold = SwipeToDismissBoxDefaults.positionalThreshold
                            val dismissState = remember(currentCode) {
                                SwipeToDismissBoxState(
                                    initialValue = SwipeToDismissBoxValue.Settled,
                                    positionalThreshold = threshold
                                )
                            }

                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                onDismiss = { direction ->
                                    if (direction == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.onToggleCurrency(currentCode, false)
                                    }
                                },
                                backgroundContent = {
                                    val isDismissing = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                                    val color = if (isDismissing) Color.Red.copy(alpha = 0.8f) else Color.Transparent
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .background(color, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        if (isDismissing) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                        }
                                    }
                                },
                                content = {
                                    ConversionCard(
                                        rowState = rowState,
                                        isDragging = isDragging,
                                        onRowFocus = { code -> viewModel.onRowFocused(code) },
                                        onAmountChange = { input -> viewModel.onAmountInputChanged(input) },
                                        onFinishInput = { viewModel.onFinishInput() },
                                        onCurrencyClick = { showCurrencySheet = true },
                                    )
                                }
                            )
                        }
                    }

                    // Bottom Action Buttons
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // "+ Add currency" button
                            Button(
                                onClick = { showCurrencySheet = true },
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Add Currency",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                )
                            }

                            // "Mid-market rates ⓘ" button
                            TextButton(
                                onClick = { showRatesInfoDialog = true },
                                modifier = Modifier
                            ) {
                                Text(
                                    text = "Mid-market rates",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Mid-market info",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Sheets and Dialogs
    if (showCurrencySheet) {
        CurrencySelectorSheet(
            currencies = allCurrenciesWithSelection,
            searchQuery = uiState.searchQuery,
            sheetState = sheetState,
            onSearchChange = { viewModel.onSearchQueryChanged(it) },
            onToggleCurrency = { code, isSelected ->
                viewModel.onToggleCurrency(code, isSelected)
            },
            onDismiss = { showCurrencySheet = false }
        )
    }

    if (showRatesInfoDialog) {
        RatesInfoDialog(
            onDismiss = { showRatesInfoDialog = false }
        )
    }
}
