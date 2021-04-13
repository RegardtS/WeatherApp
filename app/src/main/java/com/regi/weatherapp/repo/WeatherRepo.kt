package com.regi.weatherapp.repo

import com.regi.weatherapp.data.remote.response.WeatherApiResponse
import com.regi.weatherapp.other.Resource

interface WeatherRepo {

    suspend fun getWeather(lat: Double, lon: Double, unitPref: String = "metric") : Resource<WeatherApiResponse>

}