package com.hybridApp.sample.data.service

import android.util.Log
import com.hybridApp.sample.domain.model.ErrorModel
import com.hybridApp.sample.domain.model.ErrorType
import com.hybridApp.sample.domain.model.ResultDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.io.StringReader
import javax.inject.Inject

class KtorWeatherService @Inject constructor(
    private val httpClient: HttpClient
) : WeatherService {
    override suspend fun getWeatherData(): ResultDto {
        return try {
            val response =
                httpClient.get<HttpResponse>("https://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=4374531000")

            if (response.status.isSuccess()) {
                val weatherResponse = response.receive<String>()
                val weather = WeatherXmlParser().parse(StringReader(weatherResponse))
                //val weather = WeatherXmlParser().parse(weatherResponse.byteInputStream())

                if (weather == null) {
                    ResultDto.Error(ErrorModel.of(ErrorType.WEATHER_DATA_ERROR, weatherResponse))
                } else {
                    ResultDto.Success(weather, 0)
                }
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
            Log.e("belleforet", "${Log.getStackTraceString(e)}")
            ResultDto.Exception(e)
        }
    }
}