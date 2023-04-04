package com.hybridApp.sample.data.repository

import android.util.Log
import com.hybridApp.sample.data.datasource.PreferenceDataSource
import com.hybridApp.sample.data.datasource.RemoteDataSource
import com.hybridApp.sample.data.model.Constant
import com.hybridApp.sample.domain.model.MenuItem
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.domain.repository.MenuRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class MenuRepositoryImpl @Inject constructor(
    private val preferenceDataSource: PreferenceDataSource,
    private val remoteDataSource: RemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MenuRepository {

    override suspend fun getMenuList(): ResultDto {

        try {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }

            val result = withContext(ioDispatcher) {
                remoteDataSource.getData()
            }
            when (result) {
                is ResultDto.Success<*> -> {
                    val resultData = result.data as? List<*> ?: return result

                    // save menu list to preference
                    @Suppress("UNCHECKED_CAST")
                    //val menuList = resultData.filterIsInstance<MenuItem>()
                    val menuList = resultData as List<MenuItem>
                    val menuListJson = json.encodeToString(menuList)    // List<MenuItem> to json

                    withContext(ioDispatcher) {
                        preferenceDataSource.saveData(Constant.KEY_MENU, menuListJson)
                    }

                    return result
                }
                is ResultDto.Error -> {
                    // logging error
                    Log.e("belleforet", "error {${result.error.code}, ${result.error.msg}}")

                    // load menu list from preference
                    val jsonResult = withContext(ioDispatcher) {
                        preferenceDataSource.getData(Constant.KEY_MENU, null)
                    }
                    return when (jsonResult) {
                        is ResultDto.Success<*> -> {
                            // json to List<MenuItem>
                            val menuList =
                                json.decodeFromString<List<MenuItem>>(jsonResult.data as String)
                            ResultDto.Success(menuList, 0)
                        }
                        else -> {
                            result
                        }
                    }
                }
                is ResultDto.Exception -> {
                    // load from preference
                    val jsonResult = withContext(ioDispatcher) {
                        preferenceDataSource.getData(Constant.KEY_MENU, null)
                    }
                    return when (jsonResult) {
                        is ResultDto.Success<*> -> {
                            // json to List<MenuItem>
                            val menuList =
                                json.decodeFromString<List<MenuItem>>(jsonResult.data as String)
                            ResultDto.Success(menuList, 0)
                        }
                        else -> {
                            result
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return ResultDto.Exception(e)
        }

    }

}