package com.weather.app.domain.model

data class Weather(
    val cityId: String,
    val cityName: String,
    val temp: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDir: String,
    val pressure: Int,
    val visibility: Int,
    val uvIndex: Double,
    val condition: String,
    val conditionCode: String,
    val updateTime: String,
    val isCurrentLocation: Boolean = false,
    val forecast: List<Forecast> = emptyList()
)

data class Forecast(
    val date: String,
    val tempMax: Double,
    val tempMin: Double,
    val condition: String,
    val conditionCode: String
)

data class City(
    val id: String,
    val name: String,
    val country: String,
    val isCurrentLocation: Boolean = false
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
