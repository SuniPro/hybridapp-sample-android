package com.hybridApp.sample.expandablemenu

import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.hybridApp.sample.R
import com.hybridApp.sample.domain.model.ExpandableMenuItem
import javax.inject.Inject

class MenuInflater @Inject constructor(val inflater: LayoutInflater, val topLayout: LinearLayout, val subLayout: LinearLayout, val list: ArrayList<ExpandableMenuItem>, val viewModel: ExpandableMenuViewModel) {

    private val topRow = ArrayList<ConstraintLayout>()
    init {
        makeRow()
    }
    private fun setRowBg() {
        for (i in topRow.indices) {
            val row = topRow[i]
            if (i == 0) row.setBackgroundColor(Color.WHITE) //처음엔 벨포레 메뉴 활성화
            row.setBackgroundColor(Color.parseColor("#F5F5F5"))
            val tv = row.findViewById<TextView>(R.id.tv_menu_name)
            if (i == 0) tv.setTextColor(Color.parseColor("#00A9B7")) //처음엔 벨포레 메뉴 활성화
            tv.setTextColor(Color.parseColor("#404745"))
        }
    }

    private fun makeRow() {
        topLayout.removeAllViews()
        topRow.removeAll(topRow.toSet())
        for (i in list.indices) {
            val row = inflater.inflate(R.layout.top_menu_item_block, null, false) as ConstraintLayout
            val tv = row.findViewById<TextView>(R.id.tv_menu_name)
            val item = list[i]
            tv.text = item.menuNm
            topRow.add(row)
            topLayout.addView(row)
            if (item.menuUrl != null && item.menuUrl!!.isNotEmpty()) {
                row.setOnClickListener { viewModel.setState(ExpandableMenuContract.State.SelectUrl(MsgBox(item.menuUrl!!.trim(), System.currentTimeMillis()))) }
            } else {
                /* 처음엔 벨포레 메뉴 활성화 */
                if (i == 0) {
                    setRowBg()
                    row.setBackgroundColor(Color.WHITE)
                    tv.setTextColor(Color.parseColor("#00A9B7"))
                    val subList = item.childMenu
                    subLayout.removeAllViews()
                    for (j in subList.indices) {
                        val subRow = inflater.inflate(R.layout.sub_menu_item_block, null, false) as ConstraintLayout
                        val subTv = subRow.findViewById<TextView>(R.id.tv_menu_name)
                        val subItem = subList[j]
                        subTv.text = subItem.menuNm
                        subLayout.addView(subRow)
                        subRow.setOnClickListener { viewModel.setState(ExpandableMenuContract.State.SelectUrl(MsgBox(subItem.menuUrl!!.trim(), System.currentTimeMillis()))) }
                    }
                }
                row.setOnClickListener {
                    setRowBg()
                    row.setBackgroundColor(Color.WHITE)
                    tv.setTextColor(Color.parseColor("#00A9B7"))
                    val subList = item.childMenu
                    subLayout.removeAllViews()
                    for (j in subList.indices) {
                        val subRow = inflater.inflate(R.layout.sub_menu_item_block, null, false) as ConstraintLayout
                        val subTv = subRow.findViewById<TextView>(R.id.tv_menu_name)
                        val subItem = subList[j]
                        subTv.text = subItem.menuNm
                        subLayout.addView(subRow)
                        subRow.setOnClickListener { viewModel.setState(ExpandableMenuContract.State.SelectUrl(MsgBox(subItem.menuUrl!!.trim(), System.currentTimeMillis()))) }
                    }
                }
            }
        }
    }
}