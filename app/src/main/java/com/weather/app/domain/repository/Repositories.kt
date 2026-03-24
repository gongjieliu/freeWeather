package com.weather.app.domain.repository

import com.weather.app.domain.model.City
import com.weather.app.domain.model.Forecast
import com.weather.app.domain.model.Location
import com.weather.app.domain.model.Weather
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    suspend fun getWeather(locationId: String, isCurrentLocation: Boolean): Result<Weather>
    suspend fun getWeatherWithForecast(locationId: String, isCurrentLocation: Boolean): Result<Pair<Weather, List<Forecast>>>
    suspend fun searchCities(keyword: String): Result<List<City>>
    suspend fun getCityByLocation(latitude: Double, longitude: Double): Result<City>
}

interface CityRepository {
    fun getAllCities(): Flow<List<City>>
    suspend fun addCity(city: City)
    suspend fun removeCity(cityId: String)
    suspend fun updateCityOrder(cities: List<City>)
    suspend fun getCityById(id: String): City?
}

interface SettingsRepository {
    fun getLanguage(): Flow<String>
    fun getTemperatureUnit(): Flow<String>
    fun getApiKey(): Flow<String>
    suspend fun setLanguage(language: String)
    suspend fun setTemperatureUnit(unit: String)
    suspend fun setApiKey(apiKey: String)
}
