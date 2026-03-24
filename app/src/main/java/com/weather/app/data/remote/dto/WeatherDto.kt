package com.weather.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "code") val code: String,
    @Json(name = "updateTime") val updateTime: String,
    @Json(name = "fxLink") val fxLink: String,
    @Json(name = "now") val now: NowWeather?,
    @Json(name = "location") val location: List<LocationInfo>?,
    @Json(name = "daily") val forecast: List<ForecastDay>?,
    @Json(name = "topCityList") val topCityList: List<LocationInfo>?
)

@JsonClass(generateAdapter = true)
data class NowWeather(
    @Json(name = "obsTime") val obsTime: String,
    @Json(name = "temp") val temp: String,
    @Json(name = "feelsLike") val feelsLike: String,
    @Json(name = "pressure") val pressure: String,
    @Json(name = "humidity") val humidity: String,
    @Json(name = "vis") val vis: String,
    @Json(name = "windDir") val windDir: String,
    @Json(name = "windSpeed") val windSpeed: String,
    @Json(name = "windScale") val windScale: String,
    @Json(name = "icon") val condCode: String,
    @Json(name = "text") val condTxt: String,
    @Json(name = "precip") val precip: String?,
    @Json(name = "cloud") val cloud: String?,
    @Json(name = "dew") val dew: String?
)

@JsonClass(generateAdapter = true)
data class LocationInfo(
    @Json(name = "name") val name: String,
    @Json(name = "id") val id: String,
    @Json(name = "lat") val lat: String,
    @Json(name = "lon") val lon: String,
    @Json(name = "adm2") val adm2: String,
    @Json(name = "adm1") val adm1: String,
    @Json(name = "country") val country: String,
    @Json(name = "tz") val tz: String,
    @Json(name = "utcOffset") val utcOffset: String,
    @Json(name = "isDst") val isDst: String,
    @Json(name = "type") val type: String,
    @Json(name = "rank") val rank: String,
    @Json(name = "fallback") val fallback: String?
)

@JsonClass(generateAdapter = true)
data class ForecastDay(
    @Json(name = "fxDate") val fxDate: String,
    @Json(name = "sunrise") val sunrise: String?,
    @Json(name = "sunset") val sunset: String?,
    @Json(name = "moonrise") val moonrise: String?,
    @Json(name = "moonset") val moonset: String?,
    @Json(name = "moonPhase") val moonPhase: String?,
    @Json(name = "moonPhaseIcon") val moonPhaseIcon: String?,
    @Json(name = "tempMax") val tempMax: String,
    @Json(name = "tempMin") val tempMin: String,
    @Json(name = "iconDay") val iconDay: String,
    @Json(name = "textDay") val textDay: String,
    @Json(name = "iconNight") val iconNight: String,
    @Json(name = "textNight") val textNight: String,
    @Json(name = "windSpeedMax") val windSpeedMax: String?,
    @Json(name = "windDirMax") val windDirMax: String?,
    @Json(name = "windScaleMax") val windScaleMax: String?,
    @Json(name = "precip") val precip: String?,
    @Json(name = "uvIndex") val uvIndex: String?,
    @Json(name = "vis") val vis: String?,
    @Json(name = "humidity") val humidity: String?
)

@JsonClass(generateAdapter = true)
data class SearchCityResponse(
    @Json(name = "code") val code: String,
    @Json(name = "location") val location: List<LocationInfo>
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "code") val code: String,
    @Json(name = "location") val location: List<LocationInfo>
)
