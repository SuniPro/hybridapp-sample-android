package com.hybridApp.sample.domain.model

sealed class ResultDto {

    class Success<T>(val data: T, val code: Int) : ResultDto()

    //class Error(val message: String, val code: Int) : ResultDto()
    class Error(val error: ErrorModel) : ResultDto()

    class Exception(val throwable: Throwable) : ResultDto()

}
