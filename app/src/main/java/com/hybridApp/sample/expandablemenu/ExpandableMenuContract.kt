package com.hybridApp.sample.expandablemenu

import com.hybridApp.sample.base.UiEffect
import com.hybridApp.sample.base.UiEvent
import com.hybridApp.sample.base.UiState
import com.hybridApp.sample.domain.model.ExpandableMenuItem

class ExpandableMenuContract {

    sealed class Event : UiEvent {
        object LoadMenuList : Event()
        data class OnItemClicked(
            val currentList: List<ExpandableMenuItem>,
            val position: Int
        ) : Event()
    }

    sealed class State : UiState {
        object Idle : State()
        object Loading : State()
        data class Success(val menuList: ArrayList<ExpandableMenuItem>) : State()
        data class SelectUrl(val msgBox: MsgBox) : State()
    }

    sealed class Effect : UiEffect {
        data class ShowToast(val msg: String) : Effect()
        data class Exception(val err: Throwable) : Effect()
    }
}