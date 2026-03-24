package com.weather.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val language: String = "zh",
    val temperatureUnit: String = "celsius",
    val apiKey: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            launch {
                settingsRepository.getLanguage().collect { language ->
                    _uiState.value = _uiState.value.copy(language = language)
                }
            }
            launch {
                settingsRepository.getTemperatureUnit().collect { unit ->
                    _uiState.value = _uiState.value.copy(temperatureUnit = unit)
                }
            }
            launch {
                settingsRepository.getApiKey().collect { apiKey ->
                    _uiState.value = _uiState.value.copy(apiKey = apiKey)
                }
            }
        }
    }
    
    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }
    
    fun setTemperatureUnit(unit: String) {
        viewModelScope.launch {
            settingsRepository.setTemperatureUnit(unit)
        }
    }
    
    fun setApiKey(apiKey: String) {
        viewModelScope.launch {
            settingsRepository.setApiKey(apiKey)
        }
    }
}
