package com.hybridApp.sample.domain.model

class ErrorModel(
    val code: String,
    val msg: String,
    var data: String = ""
) {

    companion object {
        fun of(error: ErrorType): ErrorModel {
            return ErrorModel(error.code, error.msg)
        }

        fun of(error: ErrorType, data: String): ErrorModel {
            return ErrorModel(error.code, error.msg, data)
        }
    }
}