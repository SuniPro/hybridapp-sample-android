package com.hybridApp.sample.data.repository

import com.hybridApp.sample.data.datasource.PreferenceDataSource
import com.hybridApp.sample.data.model.Constant
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.domain.model.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val preferenceDataSource: PreferenceDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun login(user: User): ResultDto =
        try {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            val userJson = json.encodeToString(user)

            withContext(ioDispatcher) {
                preferenceDataSource.saveData(Constant.KEY_USER, userJson)
                preferenceDataSource.saveBooleanData(Constant.KEY_IS_LOGIN, true)
            }

            ResultDto.Success(user, 0)
        } catch (e: Exception) {
            ResultDto.Exception(e)
        }

    suspend fun logout() = withContext(ioDispatcher) {
        preferenceDataSource.saveData(Constant.KEY_USER, "")
        preferenceDataSource.saveBooleanData(Constant.KEY_IS_LOGIN, false)
    }

    suspend fun checkedPrivacyPolicy() = withContext(ioDispatcher) {
        preferenceDataSource.saveBooleanData(Constant.KEY_IS_CHECKED_PRIVACY_POLICY, true)
    }

    fun isCheckedPrivacyPolicy() =
        preferenceDataSource.getBooleanData(Constant.KEY_IS_CHECKED_PRIVACY_POLICY, false)

    fun isLogin() = preferenceDataSource.getBooleanData(Constant.KEY_IS_LOGIN, false)

    suspend fun getUser(): ResultDto =
        try {
            val result = withContext(ioDispatcher) {
                preferenceDataSource.getData(Constant.KEY_USER, null)
            }
            when (result) {
                is ResultDto.Success<*> -> {
                    val json = Json {
                        prettyPrint = true
                        ignoreUnknownKeys = true
                    }
                    val user = json.decodeFromString<User>(result.data as String)
                    ResultDto.Success(user, 0)
                }
                else -> {
                    result
                }
            }
        } catch (e: Exception) {
            ResultDto.Exception(e)
        }

    suspend fun getUserInfo(): User? =
        try {
            when (val result = getUser()) {
                // 타입 검증
                is ResultDto.Success<*> -> {
                    result.data as User
                }
                else -> {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
}