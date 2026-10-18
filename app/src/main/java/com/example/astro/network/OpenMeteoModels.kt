package com.example.astro.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @field:Json(name = "latitude") val latitude: Double,
    @field:Json(name = "longitude") val longitude: Double,
    @field:Json(name = "hourly") val hourly: OpenMeteoHourly?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    @field:Json(name = "time") val time: List<String>?,
    @field:Json(name = "cloudcover") val cloudCover: List<Int>?,
    @field:Json(name = "cloudcover_low") val cloudCoverLow: List<Int>?,
    @field:Json(name = "cloudcover_mid") val cloudCoverMid: List<Int>?,
    @field:Json(name = "cloudcover_high") val cloudCoverHigh: List<Int>?,
    @field:Json(name = "temperature_2m") val temperature2m: List<Float>?,
    @field:Json(name = "relativehumidity_2m") val relativeHumidity2m: List<Int>?,
    @field:Json(name = "dewpoint_2m") val dewpoint2m: List<Float>?,
    @field:Json(name = "windspeed_10m") val windspeed10m: List<Float>?,
    @field:Json(name = "visibility") val visibility: List<Float>?
)
