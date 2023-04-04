package com.hybridApp.sample.weather

import com.hybridApp.sample.base.UiEffect
import com.hybridApp.sample.base.UiEvent
import com.hybridApp.sample.base.UiState
import com.hybridApp.sample.data.model.Weather

class WeatherContract {

    sealed class Event : UiEvent {
        object Load : Event()
    }

    sealed class State : UiState {
        object Idle : State()
        object Loading : State()
        data class Success(val weather: Weather) : State()
    }

    sealed class Effect : UiEffect {
        data class ShowToast(val msg: String) : Effect()
        data class Exception(val err: Throwable) : Effect()
    }

}
