package com.fusion_nex_gen.yasuorvadapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


open class YasuoNormalVH(private val convertView: View) :
    RecyclerView.ViewHolder(convertView) {

    internal lateinit var binding: ViewBinding

    fun <T : View> getView(viewId: Int): T {
        val view = convertView.findViewById<T>(viewId)
        if (view != null) {
            return view
        }
        throw Exception("The specified view id is not found")
    }
}