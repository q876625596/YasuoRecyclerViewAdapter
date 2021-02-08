package com.fusion_nex_gen.yasuorvadapter.holder

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class YasuoDataBindingVH<out VDB : ViewDataBinding>(val binding: VDB) :
    RecyclerView.ViewHolder(binding.root) {

    fun <V : View> getView(viewId: Int): V {
        return binding.root.findViewById(viewId)
    }

    inline fun <reified VB> getBinding(): VB {
        return binding as VB
    }
}