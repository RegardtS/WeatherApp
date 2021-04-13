package com.regi.weatherapp.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.regi.weatherapp.data.remote.WeatherApi
import com.regi.weatherapp.other.Constants.BASE_URL
import com.regi.weatherapp.repo.OpenWeatherRepo
import com.regi.weatherapp.repo.WeatherRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideWeatherApi(): WeatherApi {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(WeatherApi::class.java)
    }

    @Singleton
    @Provides
    fun provideDefaultWeatherRepo(
        api: WeatherApi
    ) = OpenWeatherRepo(api) as WeatherRepo

    @Singleton
    @Provides
    fun provideFusedLocation(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}