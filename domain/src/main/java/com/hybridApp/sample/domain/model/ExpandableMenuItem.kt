package com.hybridApp.sample.domain.model

data class ExpandableMenuItem(
    val type: Int,                  // PARENT, CHILD
    var state: Int = COLLAPSED,     // COLLAPSED, EXPANDED
    val menuCd: String,
    val menuNm: String,
    val menuUrl: String?,
    val menuLvl: String,
    val menuParntsCd: String?,
    val childMenu: ArrayList<ExpandableMenuItem> = arrayListOf<ExpandableMenuItem>()
) {
    companion object {
        const val COLLAPSED = 0
        const val EXPANDED = 1
        const val PARENT = 0
        const val CHILD = 1
    }
}
