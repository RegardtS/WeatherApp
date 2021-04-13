package com.regi.weatherapp.data.remote

import com.regi.weatherapp.BuildConfig
import com.regi.weatherapp.data.remote.response.WeatherApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("weather")
    suspend fun getWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") appId: String = BuildConfig.API_KEY,
        @Query("units") unitPreference: String = "metric"
    ): Response<WeatherApiResponse>
}