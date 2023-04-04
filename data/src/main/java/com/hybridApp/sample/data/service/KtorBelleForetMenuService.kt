package com.hybridApp.sample.data.service

import com.hybridApp.sample.data.model.MenuListResponse
import com.hybridApp.sample.domain.model.ErrorModel
import com.hybridApp.sample.domain.model.ResultDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import javax.inject.Inject

class KtorBelleForetMenuService @Inject constructor(
    private val httpClient: HttpClient
) : BelleForetMenuService {
    override suspend fun getMenuList(): ResultDto {
        return try {
            val response = httpClient.get<HttpResponse>(path = "/user/menu/menuList.ajax")
            if (response.status.isSuccess()) {
                val menuListResponse = response.receive<MenuListResponse>()
                ResultDto.Success(menuListResponse.menuList, response.status.value)
            } else {
                //ResultDto.Error(response.status.description, response.status.value)
                ResultDto.Error(
                    ErrorModel(
                        response.status.value.toString(),
                        response.status.description
                    )
                )
            }
        } catch (e: Exception) {
            ResultDto.Exception(e)
        }
    }
}