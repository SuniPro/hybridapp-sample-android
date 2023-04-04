package com.hybridApp.sample.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
    val menuCd: String,
    val menuNm: String,
    val menuUrl: String?,
    val menuLvl: String,
    val menuParntsCd: String?
)
