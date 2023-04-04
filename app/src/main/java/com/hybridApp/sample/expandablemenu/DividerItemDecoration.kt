package com.hybridApp.sample.expandablemenu

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView


class DividerItemDecoration(context: Context, resId: Int) : RecyclerView.ItemDecoration() {
    /*
    private val ATTRS = intArrayOf(R.attr.listDivider)

    public DividerItemDecoration(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }
     */

    private var divider: Drawable? = ContextCompat.getDrawable(context, resId)

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left: Int = parent.paddingLeft
        val right: Int = parent.width - parent.paddingRight

        val childCount: Int = parent.childCount
        for (i in 0 until childCount) {
            val row: View = parent.getChildAt(i)
            //val noDivider = row.getTag (R.id.no_divider) as Boolean
            val noDivider = row.tag as Boolean
            if (noDivider == null || !noDivider) {
                drawDivider(canvas, row, left, right)
            }
        }
    }

    private fun drawDivider(canvas: Canvas, row: View, left: Int, right: Int) {
        val params = row.layoutParams as RecyclerView.LayoutParams
        val top = row.bottom + params.bottomMargin
        val bottom = top + divider!!.intrinsicHeight
        divider!!.setBounds(left, top, right, bottom)
        divider!!.draw(canvas)
    }

}