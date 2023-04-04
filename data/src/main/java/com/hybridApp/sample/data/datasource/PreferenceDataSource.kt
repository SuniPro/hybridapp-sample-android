package com.hybridApp.sample.data.datasource

import com.hybridApp.sample.domain.model.ResultDto

interface PreferenceDataSource {
    suspend fun getData(key: String, defValue: String?): ResultDto
    suspend fun saveData(key: String, value: String)
    fun getBooleanData(key: String, defValue: Boolean): Boolean = defValue
    suspend fun saveBooleanData(key: String, value: Boolean) {}
}