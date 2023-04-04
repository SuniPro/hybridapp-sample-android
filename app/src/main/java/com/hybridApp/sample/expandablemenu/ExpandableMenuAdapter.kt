package com.hybridApp.sample.expandablemenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hybridApp.sample.R
import com.hybridApp.sample.domain.model.ExpandableMenuItem

class ExpandableMenuAdapter(private val itemClickListener: (Int) -> Unit) :
    ListAdapter<ExpandableMenuItem, RecyclerView.ViewHolder>(diffUtil) {

    inner class ParentViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMenuName: TextView = itemView.findViewById(R.id.tv_menu_name)
        private val ivArrow: ImageView = itemView.findViewById(R.id.iv_arrow)

        fun bind(item: ExpandableMenuItem) {
            tvMenuName.text = item.menuNm

            if (item.childMenu.isNullOrEmpty()) {
                ivArrow.visibility = View.GONE
            } else {
                if (item.state == ExpandableMenuItem.EXPANDED) {
                    ivArrow.setImageResource(R.drawable.icon_arrow_2)
                } else {
                    ivArrow.setImageResource(R.drawable.icon_arrow)
                }

                ivArrow.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                itemClickListener(adapterPosition)
            }
        }
    }

    inner class ChildViewHolder(private val itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMenuName: TextView = itemView.findViewById(R.id.tv_menu_name)

        fun bind(item: ExpandableMenuItem) {
            tvMenuName.text = item.menuNm

            itemView.setOnClickListener {
                itemClickListener(adapterPosition)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            ExpandableMenuItem.PARENT -> {
                ParentViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.top_menu_item, parent, false)
                )
            }
            else -> {
                ChildViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.sub_menu_item, parent, false)
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        when (item.type) {
            ExpandableMenuItem.PARENT -> {
                (holder as ParentViewHolder).bind(item)
            }
            else -> {
                (holder as ChildViewHolder).bind(item)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ExpandableMenuItem>() {
            override fun areItemsTheSame(
                oldItem: ExpandableMenuItem,
                newItem: ExpandableMenuItem
            ): Boolean {
                return oldItem.menuCd == newItem.menuCd
            }

            override fun areContentsTheSame(
                oldItem: ExpandableMenuItem,
                newItem: ExpandableMenuItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


}