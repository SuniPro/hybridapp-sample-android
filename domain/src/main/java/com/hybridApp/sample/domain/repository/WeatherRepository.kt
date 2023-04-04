package com.hybridApp.sample.domain.repository

import com.hybridApp.sample.domain.model.ResultDto

interface WeatherRepository {
    suspend fun getWeatherData(): ResultDto
}