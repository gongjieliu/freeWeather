package com.weather.app.data.repository

import com.weather.app.data.local.CityDao
import com.weather.app.data.local.CityEntity
import com.weather.app.data.local.SettingsDataStore
import com.weather.app.data.remote.WeatherApiService
import com.weather.app.domain.model.City
import com.weather.app.domain.model.Forecast
import com.weather.app.domain.model.Location
import com.weather.app.domain.model.Weather
import com.weather.app.domain.repository.CityRepository
import com.weather.app.domain.repository.SettingsRepository
import com.weather.app.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val settingsDataStore: SettingsDataStore
) : WeatherRepository {
    
    private suspend fun getApiKey(): String {
        return settingsDataStore.apiKey.first().ifEmpty { 
            "YOUR_API_KEY" 
        }
    }
    
    override suspend fun getWeather(locationId: String, isCurrentLocation: Boolean): Result<Weather> {
        return try {
            val response = weatherApiService.getWeatherNow(locationId)
            val now = response.now ?: return Result.failure(Exception("Weather data not available"))
            
            Result.success(
                Weather(
                    cityId = locationId,
                    cityName = now.condTxt,
                    temp = now.temp.toDoubleOrNull() ?: 0.0,
                    feelsLike = now.feelsLike.toDoubleOrNull() ?: 0.0,
                    humidity = now.humidity.toIntOrNull() ?: 0,
                    windSpeed = now.windSpeed.toDoubleOrNull() ?: 0.0,
                    windDir = now.windDir,
                    pressure = now.pressure.toIntOrNull() ?: 0,
                    visibility = now.vis.toIntOrNull() ?: 0,
                    uvIndex = 0.0,
                    condition = now.condTxt,
                    conditionCode = now.condCode,
                    updateTime = response.updateTime,
                    isCurrentLocation = isCurrentLocation
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWeatherWithForecast(locationId: String, isCurrentLocation: Boolean): Result<Pair<Weather, List<Forecast>>> {
        return try {
            android.util.Log.d("WeatherRepo", "getWeatherWithForecast for locationId: $locationId")
            val nowResponse = weatherApiService.getWeatherNow(locationId)
            android.util.Log.d("WeatherRepo", "nowResponse: code=${nowResponse.code}")
            
            if (nowResponse.code != "200") {
                android.util.Log.e("WeatherRepo", "FAILED: API code is ${nowResponse.code}")
                return Result.failure(Exception("API error: ${nowResponse.code}"))
            }
            val forecastResponse = weatherApiService.getWeatherForecast(locationId)
            android.util.Log.d("WeatherRepo", "forecastResponse: code=${forecastResponse.code}")
            
            if (forecastResponse.code != "200") {
                android.util.Log.e("WeatherRepo", "FAILED: Forecast API code is ${forecastResponse.code}")
            }
            
            val now = nowResponse.now
            if (now == null) {
                android.util.Log.e("WeatherRepo", "now is null in response")
                return Result.failure(Exception("Weather data not available"))
            }
            
            val weather = Weather(
                cityId = locationId,
                cityName = now.condTxt,
                temp = now.temp.toDoubleOrNull() ?: 0.0,
                feelsLike = now.feelsLike.toDoubleOrNull() ?: 0.0,
                humidity = now.humidity.toIntOrNull() ?: 0,
                windSpeed = now.windSpeed.toDoubleOrNull() ?: 0.0,
                windDir = now.windDir,
                pressure = now.pressure.toIntOrNull() ?: 0,
                visibility = now.vis.toIntOrNull() ?: 0,
                uvIndex = 0.0,
                condition = now.condTxt,
                conditionCode = now.condCode,
                updateTime = nowResponse.updateTime,
                isCurrentLocation = isCurrentLocation
            )
            
            val forecasts = forecastResponse.forecast?.map { day ->
                Forecast(
                    date = day.fxDate,
                    tempMax = day.tempMax.toDoubleOrNull() ?: 0.0,
                    tempMin = day.tempMin.toDoubleOrNull() ?: 0.0,
                    condition = day.textDay,
                    conditionCode = day.iconDay
                )
            } ?: emptyList()
            
            Result.success(Pair(weather, forecasts))
        } catch (e: Exception) {
            android.util.Log.e("WeatherRepo", "EXCEPTION: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun searchCities(keyword: String): Result<List<City>> {
        return try {
            val response = weatherApiService.searchCity(keyword)
            
            val cities = response.location.map { location ->
                City(
                    id = location.id,
                    name = location.name,
                    country = location.country
                )
            }
            
            Result.success(cities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCityByLocation(latitude: Double, longitude: Double): Result<City> {
        return try {
            val response = weatherApiService.reverseGeocoding("$longitude,$latitude")
            val location = response.location.firstOrNull()
            
            if (location != null) {
                Result.success(
                    City(
                        id = location.id,
                        name = location.name,
                        country = location.country,
                        isCurrentLocation = true
                    )
                )
            } else {
                Result.failure(Exception("Location not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class CityRepositoryImpl @Inject constructor(
    private val cityDao: CityDao
) : CityRepository {
    
    override fun getAllCities(): Flow<List<City>> {
        android.util.Log.d("CityRepository", "getAllCities called")
        return cityDao.getAllCities().map { entities ->
            android.util.Log.d("CityRepository", "Found ${entities.size} cities in database")
            entities.map { entity ->
                City(
                    id = entity.id,
                    name = entity.name,
                    country = entity.country,
                    isCurrentLocation = entity.isCurrentLocation
                )
            }
        }
    }
    
    override suspend fun addCity(city: City) {
        android.util.Log.d("CityRepository", "addCity called: ${city.name}")
        val count = cityDao.getCityCount()
        android.util.Log.d("CityRepository", "Current city count: $count")
        cityDao.insertCity(
            CityEntity(
                id = city.id,
                name = city.name,
                country = city.country,
                isCurrentLocation = city.isCurrentLocation,
                displayOrder = count
            )
        )
        android.util.Log.d("CityRepository", "City added: ${city.name}")
    }
    
    override suspend fun removeCity(cityId: String) {
        cityDao.deleteCityById(cityId)
    }
    
    override suspend fun updateCityOrder(cities: List<City>) {
        cities.forEachIndexed { index, city ->
            val entity = cityDao.getCityById(city.id)
            entity?.let {
                cityDao.updateCity(it.copy(displayOrder = index))
            }
        }
    }
    
    override suspend fun getCityById(id: String): City? {
        return cityDao.getCityById(id)?.let { entity ->
            City(
                id = entity.id,
                name = entity.name,
                country = entity.country,
                isCurrentLocation = entity.isCurrentLocation
            )
        }
    }
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {
    
    override fun getLanguage(): Flow<String> = settingsDataStore.language
    
    override fun getTemperatureUnit(): Flow<String> = settingsDataStore.temperatureUnit
    
    override fun getApiKey(): Flow<String> = settingsDataStore.apiKey
    
    override suspend fun setLanguage(language: String) {
        settingsDataStore.setLanguage(language)
    }
    
    override suspend fun setTemperatureUnit(unit: String) {
        settingsDataStore.setTemperatureUnit(unit)
    }
    
    override suspend fun setApiKey(apiKey: String) {
        settingsDataStore.setApiKey(apiKey)
    }
}
