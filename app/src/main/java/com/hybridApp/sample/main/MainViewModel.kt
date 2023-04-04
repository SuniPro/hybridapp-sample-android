package com.hybridApp.sample.main

import androidx.lifecycle.viewModelScope
import com.hybridApp.sample.base.BaseViewModel
import com.hybridApp.sample.data.repository.UserRepository
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.domain.model.User
import com.hybridApp.sample.util.DLog
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel<MainContract.Event, MainContract.State, MainContract.Effect>() {

    override fun createInitialState(): MainContract.State {
        //return MainContract.State.Idle
        return MainContract.State.Logout
    }

    override fun handleEvent(event: MainContract.Event) {
        when (event) {
            is MainContract.Event.OnOpenDrawer -> {
                onOpenDrawer(event.timeStamp)
            }
            is MainContract.Event.LoadLoginState -> {
                loadLoginState()
            }
            is MainContract.Event.OnLogout -> {
                onLogout()
            }
            is MainContract.Event.OnLogin -> {
                onLogin(event.user)
            }
            is MainContract.Event.OnBasketCount -> {
                onBasketCount(event.count)
            }
            is MainContract.Event.OnMyTicketCount -> {
                onMyTicketCount(event.count)
            }
            is MainContract.Event.OnPageTitle -> {
                onPageTitle(event.title)
            }
            is MainContract.Event.OnPageMenuIcon -> {
                onPageIcon(event.icon)
            }
            is MainContract.Event.OnStartFullPagePopup -> {
                onStartFullPagePopup(event.url)
            }

        }
    }

    fun onCheckedPrivacyPolicy() {
        viewModelScope.launch {
            userRepository.checkedPrivacyPolicy()
        }
    }

    fun isCheckedPrivacyPolicy(): Boolean = userRepository.isCheckedPrivacyPolicy()
    fun isLogin(): Boolean = userRepository.isLogin()

    private fun onOpenDrawer(timeStamp: Long) {
        viewModelScope.launch { setState(MainContract.State.OpenDrawer(timeStamp)) }
    }

    private fun onStartFullPagePopup(url: String) {
        viewModelScope.launch { setState(MainContract.State.StartFullPagePopup(url)) }
    }

    private fun loadLoginState() {
        DLog.d("")
        viewModelScope.launch {
            try {
                if (userRepository.isLogin().not()) {
                    setState(MainContract.State.Logout)
                    return@launch
                }
                when (val result = userRepository.getUser()) {
                    is ResultDto.Success<*> -> {
                        setState(MainContract.State.Login(result.data as User))
                    }
                    is ResultDto.Error -> {
                        setState(MainContract.State.Logout)
                    }
                    is ResultDto.Exception -> {
                        setState(MainContract.State.Logout)
                        setEffect { MainContract.Effect.Exception(result.throwable) }
                    }
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    private fun onBasketCount(count: Int) {
        viewModelScope.launch { setState(MainContract.State.BasketCount(count)) }
    }

    private fun onMyTicketCount(count: Int) {
        viewModelScope.launch { setState(MainContract.State.MyTicketCount(count)) }
    }

    private fun onLogin(user: User) {
        viewModelScope.launch {
            val result = userRepository.login(user)
            DLog.d("result=${result.toString()}")
            setState(MainContract.State.Login(user))

            /*setState(
                MainContract.State.Login(
                    User(
                        "test", "김태희", "콘도그린회원"
                        //"test", "김태희", "콘도레드회원"
                        //"test", "김태희", "온라인회원"
                    )
                )
            )*/
        }
    }

    private fun onLogout() {
        viewModelScope.launch {
            userRepository.logout()
            setState(MainContract.State.Logout)
        }
    }

    private fun onPageTitle(title: String?) {
        viewModelScope.launch { setState(MainContract.State.PageTitle(title)) }
    }

    private fun onPageIcon(icon: String) {
        viewModelScope.launch { setState(MainContract.State.PageMenuIcon(icon)) }
    }

    suspend fun getUserInfo(): User? {
        return userRepository.getUserInfo()
    }


}