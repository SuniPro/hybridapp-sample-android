package com.hybridApp.sample.webscreen

import com.hybridApp.sample.base.UiEffect
import com.hybridApp.sample.base.UiEvent
import com.hybridApp.sample.base.UiState

class WebContract {

    sealed class Event : UiEvent {
        data class OnReceivedTitle(val title: String?) : Event()
        data class OnPageFinished(val canGoBack: Boolean?) : Event()
    }

    sealed class State : UiState {
        object Idle : State()
        data class ReceivedTitle(val title: String?) : State()
        data class PageFinished(val canGoBack: Boolean?) : State()
    }

    sealed class Effect : UiEffect {
        data class Exception(val err: Throwable) : Effect()
    }
}