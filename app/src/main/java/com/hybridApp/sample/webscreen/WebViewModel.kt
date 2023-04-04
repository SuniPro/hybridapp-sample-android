package com.hybridApp.sample.webscreen

import androidx.lifecycle.viewModelScope
import com.hybridApp.sample.base.BaseViewModel
import kotlinx.coroutines.launch

class WebViewModel : BaseViewModel<WebContract.Event, WebContract.State, WebContract.Effect>() {

    override fun createInitialState(): WebContract.State {
        return WebContract.State.Idle
    }

    override fun handleEvent(event: WebContract.Event) {
        when (event) {
            is WebContract.Event.OnReceivedTitle -> {
                onReceivedTitle(event.title)
            }
            is WebContract.Event.OnPageFinished -> {
                onPageFinished(event.canGoBack)
            }

        }
    }


    private fun onPageFinished(canGoBack: Boolean?) {
        viewModelScope.launch {
            setState(WebContract.State.PageFinished(canGoBack))
        }
    }

    private fun onReceivedTitle(title: String?) {
        viewModelScope.launch {
            setState(WebContract.State.ReceivedTitle(title))
        }
    }
}