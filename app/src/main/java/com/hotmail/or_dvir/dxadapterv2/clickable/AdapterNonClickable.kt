package com.hotmail.or_dvir.dxadapterv2.clickable

import android.view.LayoutInflater
import android.view.ViewGroup
import com.hotmail.or_dvir.dxadapterv2.BaseSampleAdapter
import com.hotmail.or_dvir.dxadapterv2.R

class AdapterNonClickable(mItems: MutableList<ItemNonClickable>) :
    BaseSampleAdapter<ItemNonClickable, BaseSampleAdapter.ViewHolder>(mItems) {

    override fun createAdapterViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_standard, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        holder.tv.text = item.text
    }
}