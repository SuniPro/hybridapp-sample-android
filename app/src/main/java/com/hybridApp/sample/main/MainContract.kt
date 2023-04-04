package com.hybridApp.sample.main

import com.hybridApp.sample.base.UiEffect
import com.hybridApp.sample.base.UiEvent
import com.hybridApp.sample.base.UiState
import com.hybridApp.sample.domain.model.User

class MainContract {

    sealed class Event : UiEvent {
        object LoadLoginState : Event()
        object OnLogout : Event()
        data class OnLogin(val user: User) : Event()
        data class OnBasketCount(val count: Int) : Event()
        data class OnMyTicketCount(val count: Int) : Event()
        data class OnPageTitle(val title: String?) : Event()
        data class OnPageMenuIcon(val icon: String) : Event()
        data class OnStartFullPagePopup(val url: String) : Event()
        data class OnOpenDrawer(val timeStamp: Long) : Event()
    }

    sealed class State : UiState {
        object Idle : State()
        object Logout : State()
        data class Login(val user: User) : State()
        data class BasketCount(val count: Int) : State()
        data class MyTicketCount(val count: Int) : State()
        data class PageTitle(val title: String?) : State()
        data class PageMenuIcon(val icon: String) : State()
        data class StartFullPagePopup(val url: String) : State()
        data class OpenDrawer(val timeStamp: Long) : State()
    }

    sealed class Effect : UiEffect {
        data class ShowToast(val msg: String) : Effect()
        data class Exception(val err: Throwable) : Effect()
    }

}