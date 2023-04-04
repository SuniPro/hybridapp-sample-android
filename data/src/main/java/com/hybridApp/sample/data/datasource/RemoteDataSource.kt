package com.hybridApp.sample.data.datasource

import com.hybridApp.sample.domain.model.ResultDto

interface RemoteDataSource {
    suspend fun getData(): ResultDto
}