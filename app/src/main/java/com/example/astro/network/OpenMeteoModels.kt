package com.example.astro.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "hourly") val hourly: OpenMeteoHourly?
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "cloudcover") val cloudCover: List<Int>?,
    @Json(name = "cloudcover_low") val cloudCoverLow: List<Int>?,
    @Json(name = "cloudcover_mid") val cloudCoverMid: List<Int>?,
    @Json(name = "cloudcover_high") val cloudCoverHigh: List<Int>?,
    @Json(name = "temperature_2m") val temperature2m: List<Float>?,
    @Json(name = "relativehumidity_2m") val relativeHumidity2m: List<Int>?,
    @Json(name = "dewpoint_2m") val dewpoint2m: List<Float>?,
    @Json(name = "windspeed_10m") val windspeed10m: List<Float>?,
    @Json(name = "visibility") val visibility: List<Float>?
)
