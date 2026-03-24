package com.weather.app.data.remote

import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.data.remote.dto.SearchCityResponse
import com.weather.app.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    
    @GET("v7/weather/now")
    suspend fun getWeatherNow(
        @Query("location") location: String
    ): WeatherResponse
    
    @GET("v7/weather/7d")
    suspend fun getWeatherForecast(
        @Query("location") location: String
    ): WeatherResponse
    
    @GET("geo/v2/city/lookup")
    suspend fun searchCity(
        @Query("location") location: String,
        @Query("number") number: Int = 20
    ): SearchCityResponse
    
    @GET("v3/geo/reverse")
    suspend fun reverseGeocoding(
        @Query("location") location: String
    ): GeocodingResponse
}
