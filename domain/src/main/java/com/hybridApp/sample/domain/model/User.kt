package com.hybridApp.sample.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val membershipLevel: String,
    val resNo: String
)
