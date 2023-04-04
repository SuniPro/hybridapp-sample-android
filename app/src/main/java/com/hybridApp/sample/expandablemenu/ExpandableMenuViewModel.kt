package com.hybridApp.sample.expandablemenu

import androidx.lifecycle.viewModelScope
import com.hybridApp.sample.base.BaseViewModel
import com.hybridApp.sample.domain.interactor.GetMenuListUseCase
import com.hybridApp.sample.domain.model.ExpandableMenuItem
import com.hybridApp.sample.domain.model.ResultDto
import com.hybridApp.sample.util.DLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpandableMenuViewModel @Inject constructor(
    private val getMenuListUseCase: GetMenuListUseCase
) :
    BaseViewModel<ExpandableMenuContract.Event, ExpandableMenuContract.State, ExpandableMenuContract.Effect>() {

    private lateinit var menuList: ArrayList<ExpandableMenuItem>

    override fun createInitialState(): ExpandableMenuContract.State {
        return ExpandableMenuContract.State.Idle
        //return ExpandableMenuContract.State(ExpandableMenuContract.MenuState.Idle)
    }

    override fun handleEvent(event: ExpandableMenuContract.Event) {
        DLog.d("event > $event")
        when (event) {
            is ExpandableMenuContract.Event.LoadMenuList -> {
                loadMenuList()
            }
            is ExpandableMenuContract.Event.OnItemClicked -> {
                onItemClicked(event.currentList, event.position)
            }
        }
    }

    private fun loadMenuList() {
        viewModelScope.launch {
            setState(ExpandableMenuContract.State.Loading)
            //setState { copy(menuState = ExpandableMenuContract.MenuState.Loading) }

            try {
                when (val result = getMenuListUseCase.execute()) {
                    is ResultDto.Success<*> -> {
                        menuList = result.data as ArrayList<ExpandableMenuItem>
                        setState(ExpandableMenuContract.State.Success(menuList))
                    }
                    is ResultDto.Error -> {
                        setState(ExpandableMenuContract.State.Idle)
                        //setState { copy(menuState = ExpandableMenuContract.MenuState.Idle) }
                        setEffect { ExpandableMenuContract.Effect.ShowToast("[${result.error.code}] ${result.error.msg}") }

                    }
                    is ResultDto.Exception -> {
                        setState(ExpandableMenuContract.State.Idle)
                        //setState { copy(menuState = ExpandableMenuContract.MenuState.Idle) }
                        setEffect { ExpandableMenuContract.Effect.Exception(result.throwable) }
                    }
                }
            } catch (e: Exception) {
                setState(ExpandableMenuContract.State.Idle)
                //setState { copy(menuState = ExpandableMenuContract.MenuState.Idle) }
                setEffect { ExpandableMenuContract.Effect.Exception(e) }
            }
        }
    }

    private fun onItemClicked(currentList: List<ExpandableMenuItem>, position: Int) {
        DLog.d("menuName=${currentList[position].menuNm}")

        viewModelScope.launch {
            //setState(ExpandableMenuContract.State.Idle)
            setState(ExpandableMenuContract.State.Loading)

            // copy currentList
            val newList = arrayListOf<ExpandableMenuItem>()
            currentList.map { expandableMenuItem ->
                newList.add(expandableMenuItem.copy())
            }

            if (currentList[position].state == ExpandableMenuItem.COLLAPSED) {
                expandMenu(newList, position)
            } else {
                collapseMenu(newList, position)
            }
        }
    }

    private suspend fun collapseMenu(newMenuList: ArrayList<ExpandableMenuItem>, position: Int) {
        DLog.d("position=$position")
        val from = position + 1
        val to = from + newMenuList[position].childMenu.count()

        if (to >= newMenuList.size) {
            newMenuList.subList(from, newMenuList.lastIndex).clear()
            newMenuList.removeLastOrNull()
        } else {
            newMenuList.subList(from, to).clear()
        }
        newMenuList[position].state = ExpandableMenuItem.COLLAPSED
        setState(ExpandableMenuContract.State.Success(newMenuList))
    }

    private suspend fun expandMenu(newMenuList: ArrayList<ExpandableMenuItem>, position: Int) {
        val menuItem = newMenuList[position]
        DLog.d("${menuItem.toString()}")
        if (menuItem.childMenu.isNullOrEmpty()) {
            menuItem.menuUrl?.let { menuUrl ->
                DLog.d("menuUrl=$menuUrl")

                setState(
                    ExpandableMenuContract.State.SelectUrl(
                        MsgBox(
                            menuUrl.trim(),
                            System.currentTimeMillis()
                        )
                    )
                )
                //setState(ExpandableMenuContract.State.SelectUrl(menuItem.copy()))

                return
            }
            setEffect { ExpandableMenuContract.Effect.ShowToast("menuUrl is null !") }
            return
        }

        var index = position
        menuItem.childMenu.forEach { expandableMenuItem ->
            newMenuList.add(++index, expandableMenuItem)
        }
        newMenuList[position].state = ExpandableMenuItem.EXPANDED
        setState(ExpandableMenuContract.State.Success(newMenuList))
    }

}