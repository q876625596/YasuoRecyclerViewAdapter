package com.fusion_nex_gen.yasuorvadapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class RecyclerViewBindingHolder<VB : ViewBinding>(root: View) :
    RecyclerView.ViewHolder(root) {
    internal lateinit var binding: VB

    /**
     * 首次调用需要初始化binding
     */
    fun <V : ViewBinding> createBinding(createBinding: (view: View) -> VB): V {
        if (!::binding.isInitialized) {
            binding = createBinding(itemView)
        }
        return binding as V
    }
}