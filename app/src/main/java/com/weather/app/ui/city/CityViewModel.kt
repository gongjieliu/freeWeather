package com.weather.app.ui.city

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.app.domain.model.City
import com.weather.app.domain.repository.CityRepository
import com.weather.app.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CityUiState(
    val cities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CitySearchState(
    val results: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CityViewModel @Inject constructor(
    private val cityRepository: CityRepository,
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CityUiState())
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()
    
    private val _searchState = MutableStateFlow(CitySearchState())
    val searchState: StateFlow<CitySearchState> = _searchState.asStateFlow()
    
    init {
        loadCities()
    }
    
    private fun loadCities() {
        viewModelScope.launch {
            cityRepository.getAllCities().collect { cities ->
                _uiState.value = _uiState.value.copy(cities = cities)
            }
        }
    }
    
    fun searchCity(keyword: String) {
        if (keyword.isBlank()) {
            _searchState.value = CitySearchState()
            return
        }
        
        viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true, error = null)
            
            val result = weatherRepository.searchCities(keyword)
            result.onSuccess { cities ->
                _searchState.value = _searchState.value.copy(
                    results = cities,
                    isLoading = false
                )
            }.onFailure { e ->
                _searchState.value = _searchState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun addCity(city: City) {
        viewModelScope.launch {
            cityRepository.addCity(city)
        }
    }
    
    fun deleteCity(city: City) {
        viewModelScope.launch {
            cityRepository.removeCity(city.id)
        }
    }
    
    fun moveCityUp(index: Int) {
        viewModelScope.launch {
            val currentList = _uiState.value.cities.toMutableList()
            if (index > 0) {
                val item = currentList[index]
                currentList[index] = currentList[index - 1]
                currentList[index - 1] = item
                cityRepository.updateCityOrder(currentList)
            }
        }
    }
    
    fun moveCityDown(index: Int) {
        viewModelScope.launch {
            val currentList = _uiState.value.cities.toMutableList()
            if (index < currentList.size - 1) {
                val item = currentList[index]
                currentList[index] = currentList[index + 1]
                currentList[index + 1] = item
                cityRepository.updateCityOrder(currentList)
            }
        }
    }
    
    fun clearSearch() {
        _searchState.value = CitySearchState()
    }
}
