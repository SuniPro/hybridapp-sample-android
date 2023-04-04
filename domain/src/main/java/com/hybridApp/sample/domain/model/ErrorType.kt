package com.hybridApp.sample.domain.model

enum class ErrorType(
    val code: String,
    val msg: String
) {
    SUCCESS("0000", ""),
    WEATHER_DATA_ERROR("W001", "날씨 데이터를 확인하세요."),

    PREFERENCE_NO_DATA("P001", "데이터가 없습니다.")

}