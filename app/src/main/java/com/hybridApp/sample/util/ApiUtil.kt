package com.hybridApp.sample.util

import com.hybridApp.sample.data.BuildConfig
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.*

/**
 * param
 * url(경로), method(get, post), async(동기 true, 비동기 false), data
 */
class ApiUtil {
    companion object {
        private const val host = BuildConfig.HOST
        private const val flavor = BuildConfig.FLAVOR
        private val httpClient: HttpClient = HttpClient()

        /* api */
        suspend fun send(
            url: String,
            method: String,
            async: Boolean,
            data: Map<String, Objects>?
        ): Response? {
            val okHttpClient = OkHttpClient()
            //method
            val request = when (method) {
                "get" -> getMethod(url)
                "post" -> postMethod(url, data)
                else -> {
                    DLog.w("method null.")
                    getMethod(url)
                }
            }

            //결과 저장 변수
            var res: Response? = null
            if (async) {
                //동기
                withContext(Dispatchers.Default) {
                    try {
                        res = okHttpClient.newCall(request).execute()
                        DLog.i("result : " + res.toString())
                    } catch (e: IOException) {
                        DLog.e("Web Server Error.")
                    }
                }
            } else {
                //비동기
                withContext(Dispatchers.Default) {
                    okHttpClient.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            DLog.e("Web Server Error.")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            res = response
                            DLog.d("result : " + res.toString())
                        }
                    })
                }
            }

            //웹 서버 error 처리
            if (res == null) {
                DLog.e("응답 없음.")
                return null
            }

            //404 페이지 반환 error 처리
            if (res!!.code.toString() == "404") {
                DLog.e("404 error.")
                return null
            }

            return res
        }

        private fun getMethod(url: String): Request { //get방식
            return Request.Builder().url(getUrl(url)).build()
        }

        private fun postMethod(url: String, map: Map<String, Objects>?): Request { //post방삭
            val mediaType: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
            return Request.Builder().url(getUrl(url))
                .post(Gson().toJson(map).toRequestBody(mediaType)).build()
        }

        private fun getUrl(url: String): String {
            var ptc = "http://" //로컬
            if (flavor == "blackstonebelleforet" || flavor == "domain") {
                ptc = "https://" //개발, 운영
            }
            return ptc + host + url
        }

        /* 각 item(골프, 콘도, 선물함) 카운팅 */
        suspend fun itemCount(url: String): String? {
            val okHttpClient = OkHttpClient()
            val request = Request.Builder().url(getUrl(url)).build()
            var response: String?
            withContext(Dispatchers.Default) {
                response = try {
                    okHttpClient.newCall(request).execute().body?.string()
                } catch (e: Exception) {
                    DLog.e("Web Server Error.")
                    null
                }
            }
            return response
        }

        suspend fun requestMenuCount(url: String): String? {
            val data: String?
            withContext(Dispatchers.Default) {
                val response: HttpResponse = httpClient.get(getUrl(url))
                data = response.receive()
                DLog.d("requestMenuCount response : $response, data : $data")
            }
            return data
        }

    }
}