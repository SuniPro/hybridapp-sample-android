package com.hybridApp.sample.domain.interactor

import com.hybridApp.sample.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    val weatherRepository: WeatherRepository
) {
    suspend fun execute() = weatherRepository.getWeatherData()
}