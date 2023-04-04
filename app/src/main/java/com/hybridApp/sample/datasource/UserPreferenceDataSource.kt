package com.hybridApp.sample.datasource

import com.hybridApp.sample.data.datasource.PreferenceDataSource
import com.hybridApp.sample.domain.model.ErrorModel
import com.hybridApp.sample.domain.model.ErrorType
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.util.PrefManager
import javax.inject.Inject

class UserPreferenceDataSource @Inject constructor(
    val prefManager: PrefManager
) : PreferenceDataSource {
    override suspend fun getData(key: String, defValue: String?): ResultDto {
        try {
            val result = prefManager.getString(key, defValue)
            if (result.isNullOrEmpty()) {
                //return ResultDto.Error("$key data is not exist !", 9)
                return ResultDto.Error(ErrorModel.of(ErrorType.PREFERENCE_NO_DATA))
            }

            return ResultDto.Success(result, 0)
        } catch (e: Exception) {
            return ResultDto.Exception(e)
        }
    }

    override suspend fun saveData(key: String, value: String) {
        prefManager.setString(key, value)
    }

    override fun getBooleanData(key: String, defValue: Boolean): Boolean {
        return prefManager.getBoolean(key, defValue)
    }

    override suspend fun saveBooleanData(key: String, value: Boolean) {
        prefManager.setBoolean(key, value)
    }

}