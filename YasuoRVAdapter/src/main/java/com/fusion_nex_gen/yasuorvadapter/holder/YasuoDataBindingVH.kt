package com.fusion_nex_gen.yasuorvadapter.holder

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class YasuoDataBindingVH<out T : ViewDataBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root) {

    fun <T : View> getView(viewId: Int): T {
        return binding.root.findViewById(viewId)
    }

    inline fun <reified VB> getBinding(): VB {
        return binding as VB
    }
}