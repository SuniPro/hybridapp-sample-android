package com.hybridApp.sample.weather

import androidx.lifecycle.viewModelScope
import com.hybridApp.sample.base.BaseViewModel
import com.hybridApp.sample.data.model.Weather
import com.hybridApp.sample.domain.interactor.GetWeatherUseCase
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.util.DLog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase
) :
    BaseViewModel<WeatherContract.Event, WeatherContract.State, WeatherContract.Effect>() {


    override fun createInitialState(): WeatherContract.State {
        return WeatherContract.State.Idle
    }

    override fun handleEvent(event: WeatherContract.Event) {
        DLog.d("event > $event")
        when (event) {
            is WeatherContract.Event.Load -> {
                loadData()
            }
        }
    }

    private fun loadData() {
        if (getState() == WeatherContract.State.Loading) return

        viewModelScope.launch {
            setState(WeatherContract.State.Loading)

            try {
                when (val result = getWeatherUseCase.execute()) {
                    is ResultDto.Success<*> -> {
                        val weather = result.data as Weather
                        DLog.d("$weather")
                        setState(WeatherContract.State.Success(weather))
                    }

                    is ResultDto.Error -> {
                        Firebase.crashlytics.log("[${result.error.code}][${result.error.msg}] ${result.error.data}")
                        setState(WeatherContract.State.Idle)
                        setEffect { WeatherContract.Effect.ShowToast("[$result.error.code] $result.error.msg") }
                    }
                    is ResultDto.Exception -> {
                        setState(WeatherContract.State.Idle)
                        setEffect { WeatherContract.Effect.Exception(result.throwable) }
                    }
                }
            } catch (e: Exception) {
                setState(WeatherContract.State.Idle)
                setEffect { WeatherContract.Effect.Exception(e) }
            }
        }
    }

}