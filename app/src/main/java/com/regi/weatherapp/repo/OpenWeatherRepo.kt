package com.regi.weatherapp.repo

import com.regi.weatherapp.data.remote.WeatherApi
import com.regi.weatherapp.data.remote.response.WeatherApiResponse
import com.regi.weatherapp.other.Resource
import javax.inject.Inject

class OpenWeatherRepo @Inject constructor(
    private val weatherApi: WeatherApi
) : WeatherRepo {

    override suspend fun getWeather(
        lat: Double,
        lon: Double,
        unitPref: String
    ): Resource<WeatherApiResponse> {
        return try {
            val response = weatherApi.getWeather(lat, lon)
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occurred", null)
            } else {
                Resource.error(response.message() ?:"An unknown error occurred", null)
            }
        } catch (e: Exception) {
            Resource.error(e.localizedMessage ?: "An unknown error occurred", null)
        }
    }
}