package com.weather.app.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.weather.app.domain.model.City
import com.weather.app.domain.model.Forecast
import com.weather.app.domain.model.Weather
import com.weather.app.domain.repository.CityRepository
import com.weather.app.domain.repository.SettingsRepository
import com.weather.app.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class HomeUiState(
    val currentLocationWeather: Weather? = null,
    val otherCitiesWeather: List<Weather> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val temperatureUnit: String = "celsius"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val cityRepository: CityRepository,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    
    init {
        loadSettings()
        loadCities()
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getTemperatureUnit().collect { unit ->
                _uiState.value = _uiState.value.copy(temperatureUnit = unit)
            }
        }
    }
    
    private fun loadCities() {
        viewModelScope.launch {
            cityRepository.getAllCities().collect { cities ->
                android.util.Log.d("HomeViewModel", "Cities loaded: ${cities.size}")
                refreshAllWeather(cities)
            }
        }
    }
    
    fun loadCurrentLocation(activity: FragmentActivity) {
        if (!hasLocationPermission()) {
            return
        }
        
        viewModelScope.launch {
            try {
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
                val cancellationToken = CancellationTokenSource()
                
                val location = fusedLocationClient?.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationToken.token
                )?.await()
                
                location?.let {
                    val result = weatherRepository.getCityByLocation(it.latitude, it.longitude)
                    result.onSuccess { city ->
                        cityRepository.addCity(city)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun refreshAllWeather(cities: List<City>) {
        android.util.Log.d("HomeViewModel", "refreshAllWeather called with ${cities.size} cities")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val currentLocationCity = cities.find { it.isCurrentLocation }
                val otherCities = cities.filter { !it.isCurrentLocation }
                android.util.Log.d("HomeViewModel", "currentLocationCity: ${currentLocationCity?.name}, otherCities: ${otherCities.map { it.name }}")
                
                val weatherList = mutableListOf<Weather>()
                var currentLocationWeather: Weather? = null
                
                currentLocationCity?.let { city ->
                    val result = weatherRepository.getWeatherWithForecast(city.id, true)
                    result.onSuccess { (weather, forecasts) ->
                        currentLocationWeather = weather.copy(forecast = forecasts)
                    }.onFailure { e ->
                        _uiState.value = _uiState.value.copy(error = e.message)
                    }
                }
                
                val otherWeatherList = otherCities.mapNotNull { city ->
                    android.util.Log.d("HomeViewModel", "Fetching weather for: ${city.name} (${city.id})")
                    val result = weatherRepository.getWeatherWithForecast(city.id, false)
                    android.util.Log.d("HomeViewModel", "Result for ${city.name}: ${result.isSuccess}")
                    result.getOrNull()?.let { (weather, forecasts) ->
                        weather.copy(cityName = city.name, forecast = forecasts)
                    }
                }
                
                android.util.Log.d("HomeViewModel", "otherWeatherList size: ${otherWeatherList.size}")
                _uiState.value = _uiState.value.copy(
                    currentLocationWeather = currentLocationWeather,
                    otherCitiesWeather = otherWeatherList,
                    isLoading = false
                )
                android.util.Log.d("HomeViewModel", "UI state updated: otherCitiesWeather=${otherWeatherList.size}")
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error in refreshAllWeather", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun refreshWeather() {
        viewModelScope.launch {
            cityRepository.getAllCities().first().let { cities ->
                refreshAllWeather(cities)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun formatTemperature(temp: Double): String {
        val unit = _uiState.value.temperatureUnit
        return if (unit == "fahrenheit") {
            "${(temp * 9/5 + 32).toInt()}°F"
        } else {
            "${temp.toInt()}°C"
        }
    }
}
