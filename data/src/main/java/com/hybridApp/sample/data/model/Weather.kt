package com.hybridApp.sample.data.model

data class Weather(
    val temp: String,   // 온도 (C)
    val wfKor: String,  // 날씨한국어(맑음,구름 조금,구름 많음,흐림,비,눈/비,눈)
    val wfEn: String,   // 날씨영어(Clear,Partly Cloudy,Mostly Cloudy,Cloudy,Rain,Snow/Rain,Snow)
    val ws: String,     // 풍속 (m/s)
    val wd: Int,        // 풍향 0~7(북,북동,동,남동,남,남서,서,북서)
    val wdKor: String,  // 풍향한국어
    val wdEn: String,   // 풍향영어
    val zone: String = "증평군",   // 지역
)
