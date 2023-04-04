package com.hybridApp.sample.data.repository

import com.hybridApp.sample.data.datasource.RemoteDataSource
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WeatherRepository {

    override suspend fun getWeatherData(): ResultDto = withContext(ioDispatcher) {
        remoteDataSource.getData()
    }

}