package com.hybridApp.sample.domain.interactor

import com.hybridApp.sample.domain.model.MenuItem
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.domain.repository.MenuRepository
import com.hybridApp.sample.domain.toExpandableMenuList
import javax.inject.Inject

class GetMenuListUseCase @Inject constructor(
    val menuRepository: MenuRepository
) {
    suspend fun execute() =
        when (val result = menuRepository.getMenuList()) {
            is ResultDto.Success<*> -> {
                val expandableMenuList = (result.data as List<MenuItem>).toExpandableMenuList()

                ResultDto.Success(expandableMenuList, result.code)
            }
            else -> {
                result
            }
        }

}