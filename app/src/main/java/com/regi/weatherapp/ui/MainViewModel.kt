package com.regi.weatherapp.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.regi.weatherapp.data.remote.response.WeatherApiResponse
import com.regi.weatherapp.other.Resource
import com.regi.weatherapp.repo.WeatherRepo
import kotlinx.coroutines.launch

class MainViewModel @ViewModelInject constructor(
    private val repo: WeatherRepo
) : ViewModel() {

    private val _weather = MutableLiveData<Resource<WeatherApiResponse>>()
    val weather: LiveData<Resource<WeatherApiResponse>> = _weather

    fun getWeather(lat: Double, lon: Double) {
        _weather.value = Resource.loading(null)
        viewModelScope.launch {
            val response = repo.getWeather(lat, lon)
            _weather.value = response
        }
    }
}