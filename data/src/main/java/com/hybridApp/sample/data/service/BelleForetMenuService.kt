package com.hybridApp.sample.data.service

import com.hybridApp.sample.domain.model.ResultDto

interface BelleForetMenuService {
    suspend fun getMenuList(): ResultDto
}