package com.hybridApp.sample.data.service

import com.hybridApp.sample.domain.model.ResultDto

interface WeatherService {
    suspend fun getWeatherData(): ResultDto
}