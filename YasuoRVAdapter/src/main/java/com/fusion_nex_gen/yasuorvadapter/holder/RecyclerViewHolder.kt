package com.fusion_nex_gen.yasuorvadapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.getFormat


class RecyclerViewHolder(private val convertView: View) :
    RecyclerView.ViewHolder(convertView) {

    fun <T : View> getView(viewId: Int): T {
        val view = convertView.findViewById<T>(viewId)
        if (view != null) {
            return view
        }
        throw Exception("The specified view id is not found")
    }

    inline fun <T : View, D> applyView(
        viewId: Int,
        itemList: MutableList<Any>,
        crossinline block: T.(data: D) -> Unit
    ): RecyclerViewHolder {
        getView<T>(viewId).block(itemList.getFormat(bindingAdapterPosition))
        return this
    }

}