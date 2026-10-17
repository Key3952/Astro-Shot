package com.example.astro.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoService {

    @GET("v1/forecast")
    suspend fun getAstroWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "cloudcover,cloudcover_low,cloudcover_mid,cloudcover_high,temperature_2m,relativehumidity_2m,dewpoint_2m,windspeed_10m,visibility",
        @Query("forecast_days") forecastDays: Int = 3,
        @Query("timezone") timezone: String = "auto"
    ): OpenMeteoResponse
}
