package com.hybridApp.sample.domain

import com.hybridApp.sample.domain.model.ExpandableMenuItem
import com.hybridApp.sample.domain.model.MenuItem


fun List<MenuItem>.toExpandableMenuList(): ArrayList<ExpandableMenuItem> {
    val expandableMenuList = arrayListOf<ExpandableMenuItem>()

    this.filter {
        it.menuLvl == "1"
    }.forEach { menuItem ->
        expandableMenuList.add(
            ExpandableMenuItem(
                type = ExpandableMenuItem.PARENT,
                menuCd = menuItem.menuCd,
                menuNm = menuItem.menuNm,
                menuUrl = menuItem.menuUrl,
                menuLvl = menuItem.menuLvl,
                menuParntsCd = menuItem.menuParntsCd
            )
        )
    }

    expandableMenuList.forEach { expandableMenuItem ->
        this.filter {
            it.menuParntsCd == expandableMenuItem.menuCd
            //it.menuCd.startsWith(expandableMenuItem.menuCd) && it.menuCd != expandableMenuItem.menuCd
        }.forEach { menuItem ->
            expandableMenuItem.childMenu.add(
                ExpandableMenuItem(
                    type = ExpandableMenuItem.CHILD,
                    menuCd = menuItem.menuCd,
                    menuNm = menuItem.menuNm,
                    menuUrl = menuItem.menuUrl,
                    menuLvl = menuItem.menuLvl,
                    menuParntsCd = menuItem.menuParntsCd
                )
            )
        }
    }

    return expandableMenuList
}