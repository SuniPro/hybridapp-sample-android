package com.hybridApp.sample.data.datasource

import com.hybridApp.sample.data.service.BelleForetMenuService
import com.hybridApp.sample.domain.model.ResultDto
import javax.inject.Inject

class BelleForetMenuRemoteDataSource @Inject constructor(
    private val belleForetMenuService: BelleForetMenuService
) : RemoteDataSource {

    override suspend fun getData(): ResultDto = belleForetMenuService.getMenuList()

}