package com.weather.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.weather.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onLanguageChanged: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showTemperatureDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    
    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                supportingContent = {
                    Text(
                        if (uiState.language == "zh") stringResource(R.string.chinese)
                        else stringResource(R.string.english)
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null)
                },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            
            ListItem(
                headlineContent = { Text(stringResource(R.string.temperature_unit)) },
                supportingContent = {
                    Text(
                        if (uiState.temperatureUnit == "celsius") stringResource(R.string.celsius)
                        else stringResource(R.string.fahrenheit)
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Thermostat, contentDescription = null)
                },
                modifier = Modifier.clickable { showTemperatureDialog = true }
            )
            
            ListItem(
                headlineContent = { Text("API Key") },
                supportingContent = {
                    Text(
                        if (uiState.apiKey.isNotEmpty()) "******${uiState.apiKey.takeLast(4)}"
                        else "Not set"
                    )
                },
                leadingContent = {
                    Icon(Icons.Default.Check, contentDescription = null)
                },
                modifier = Modifier.clickable { showApiKeyDialog = true }
            )
            
            ListItem(
                headlineContent = { Text(stringResource(R.string.about)) },
                supportingContent = { Text("${stringResource(R.string.version)}: 1.0.0") }
            )
        }
    }
    
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    LanguageOption(
                        text = stringResource(R.string.chinese),
                        selected = uiState.language == "zh",
                        onClick = {
                            viewModel.setLanguage("zh")
                            showLanguageDialog = false
                            onLanguageChanged()
                        }
                    )
                    LanguageOption(
                        text = stringResource(R.string.english),
                        selected = uiState.language == "en",
                        onClick = {
                            viewModel.setLanguage("en")
                            showLanguageDialog = false
                            onLanguageChanged()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showTemperatureDialog) {
        AlertDialog(
            onDismissRequest = { showTemperatureDialog = false },
            title = { Text(stringResource(R.string.temperature_unit)) },
            text = {
                Column {
                    LanguageOption(
                        text = stringResource(R.string.celsius),
                        selected = uiState.temperatureUnit == "celsius",
                        onClick = {
                            viewModel.setTemperatureUnit("celsius")
                            showTemperatureDialog = false
                        }
                    )
                    LanguageOption(
                        text = stringResource(R.string.fahrenheit),
                        selected = uiState.temperatureUnit == "fahrenheit",
                        onClick = {
                            viewModel.setTemperatureUnit("fahrenheit")
                            showTemperatureDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemperatureDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentApiKey = uiState.apiKey,
            onConfirm = { apiKey ->
                viewModel.setApiKey(apiKey)
                showApiKeyDialog = false
            },
            onDismiss = { showApiKeyDialog = false }
        )
    }
}

@Composable
fun LanguageOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun ApiKeyDialog(
    currentApiKey: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKey by remember { mutableStateOf(currentApiKey) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("API Key") },
        text = {
            Column {
                Text(
                    text = "Enter your QWeather API key. Get one free at: https://dev.qweather.com/",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(apiKey) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
