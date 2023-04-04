package com.hybridApp.sample.data.datasource

import com.hybridApp.sample.data.service.WeatherService
import com.hybridApp.sample.domain.model.ResultDto
import javax.inject.Inject

class WeatherRemoteDataSource @Inject constructor(
    private val weatherService: WeatherService
) : RemoteDataSource {

    override suspend fun getData(): ResultDto {
        return weatherService.getWeatherData()
    }

}