package com.felixbrucker.currencyconverter.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.felixbrucker.currencyconverter.data.local.ExchangeRateProviderEntity
import com.felixbrucker.currencyconverter.data.remote.CurrencyEnumType
import com.felixbrucker.currencyconverter.data.remote.ExchangeRateProvider
import com.felixbrucker.currencyconverter.ui.components.IndicatorBox
import com.felixbrucker.currencyconverter.util.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ConversionViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { _ ->
            // Permission result handled by the system notification helper check
        }
    )

    var sliderValue by remember(uiState.bgSyncIntervalHours) {
        mutableFloatStateOf(uiState.bgSyncIntervalHours.toFloat())
    }

    var autoRefreshSlider by remember(uiState.autoRefreshMinutes) {
        mutableFloatStateOf(uiState.autoRefreshMinutes.toFloat())
    }

    var selectedProviderForApiKey by remember { mutableStateOf<Pair<ExchangeRateProviderEntity, ExchangeRateProvider>?>(null) }

    if (selectedProviderForApiKey != null) {
        val (providerEntity, provider) = selectedProviderForApiKey!!
        var apiKeyText by remember { mutableStateOf(providerEntity.apiKey ?: "") }
        val apiKeyIdentifier = provider.displayProperties.apiKeyIdentifier ?: "API Key"

        AlertDialog(
            onDismissRequest = { selectedProviderForApiKey = null },
            title = { Text("Configure ${provider.name}") },
            text = {
                Column {
                    Text(
                        text = "Enter your $apiKeyIdentifier for this provider. You can find it in your provider's dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        label = { Text(apiKeyIdentifier) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onUpdateProviderApiKey(provider.name, apiKeyText.ifBlank { null })
                    selectedProviderForApiKey = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProviderForApiKey = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Background Sync Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Background Rate Sync",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Periodically update rates in the background",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Switch(
                            checked = uiState.bgSyncEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (!hasPermission) {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                    viewModel.setBgSyncEnabled(true)
                                } else {
                                    viewModel.setBgSyncEnabled(false)
                                }
                            },
                            modifier = Modifier.testTag("bg_sync_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sync Interval",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = if (uiState.bgSyncEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        )
                        Text(
                            text = "${sliderValue.roundToInt()} hours",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.bgSyncEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        )
                    }

                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = {
                            viewModel.setBgSyncIntervalHours(sliderValue.roundToInt().toLong())
                        },
                        enabled = uiState.bgSyncEnabled,
                        valueRange = 1f..24f,
                        steps = 22,
                        modifier = Modifier.testTag("sync_interval_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.bgSyncEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        )
                        Text(
                            text = "12h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.bgSyncEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        )
                        Text(
                            text = "24h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (uiState.bgSyncEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Foreground Live Refresh Interval Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Live App Auto-Refresh",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "When app is open and online",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Text(
                            text = "${autoRefreshSlider.roundToInt()} min",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Slider(
                        value = autoRefreshSlider,
                        onValueChange = { autoRefreshSlider = it },
                        onValueChangeFinished = {
                            viewModel.setAutoRefreshMinutes(autoRefreshSlider.roundToInt())
                        },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.testTag("auto_refresh_slider")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Currency Providers Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Exchange Rate Providers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            )
                        )
                        Text(
                            text = "Manage exchange rate providers. Some providers require an api key.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )

                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.providers.forEachIndexed { index, (providerEntity, provider) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.clickable {
                                        uriHandler.openUri(provider.displayProperties.infoUrl)
                                    }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = providerEntity.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = "Open Info",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = if (providerEntity.lastUpdatedAt != null) {
                                            "Last data update: ${DateTimeFormatter.formatRelative(providerEntity.lastUpdatedAt)}"
                                        } else {
                                            "Never synced"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                    if (providerEntity.nextUpdateAt != null) {
                                        Text(
                                            text = "Next sync: ${DateTimeFormatter.formatRelative(providerEntity.nextUpdateAt)}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.weight(1.0f),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    if (provider.displayProperties.supportedCurrencyTypes.contains(CurrencyEnumType.Fiat)) {
                                        IndicatorBox(
                                            text = "FIAT",
                                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                            iconImageVector = Icons.Default.CurrencyExchange,
                                        )
                                    }
                                    if (provider.displayProperties.supportedCurrencyTypes.contains(CurrencyEnumType.Crypto)) {
                                        IndicatorBox(
                                            text = "CRYPTO",
                                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                            iconImageVector = Icons.Default.CurrencyExchange,
                                        )
                                    }
                                    IndicatorBox(
                                        text = "updates every ${provider.displayProperties.updateFrequency}",
                                        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                                        iconImageVector = Icons.Default.Sync,
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    if (provider.requiresApiKey) {
                                        OutlinedIconButton(
                                            onClick = {
                                                selectedProviderForApiKey = providerEntity to provider
                                            },
                                            modifier = Modifier.size(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = IconButtonDefaults.outlinedIconButtonColors(
                                                contentColor = if (providerEntity.apiKey.isNullOrBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                            ),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (providerEntity.apiKey.isNullOrBlank()) MaterialTheme.colorScheme.error.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Key,
                                                contentDescription = "Configure API Key",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = providerEntity.isEnabled,
                                        onCheckedChange = { viewModel.onToggleProvider(providerEntity.name, it) },
                                        modifier = Modifier.size(width = 44.dp, height = 24.dp)
                                    )
                                }
                            }
                            if (index < uiState.providers.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sync Status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Last synced: ${DateTimeFormatter.formatExact(uiState.lastUpdatedTimestamp)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Last backup: ${DateTimeFormatter.formatRelative(uiState.lastBackupTimestamp)}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    }
}
