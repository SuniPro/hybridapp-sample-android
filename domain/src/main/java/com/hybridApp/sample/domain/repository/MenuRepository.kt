package com.hybridApp.sample.domain.repository

import com.hybridApp.sample.domain.model.ResultDto

interface MenuRepository {
    suspend fun getMenuList(): ResultDto
}