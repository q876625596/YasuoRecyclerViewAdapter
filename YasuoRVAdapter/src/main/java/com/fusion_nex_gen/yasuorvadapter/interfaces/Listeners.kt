package com.fusion_nex_gen.yasuorvadapter.interfaces

import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding


interface Listener<in VH>

/**
 * View Holder创建时触发，用于完成视图的初始化
 *
 */
interface ViewHolderCreateListener<in VH> : Listener<VH> {
    /**
     * @param holder adapter position
     */
    fun onCreateViewHolder(holder: VH)

}

/**
 * View Holder创建时触发，用于完成视图的初始化
 *
 */
interface ViewHolderCreateListenerForViewBinding<in VH> : Listener<VH> {

    /**
     * @param holder adapter position
     * @param binding viewBinding
     */
    fun onCreateViewHolder(holder: VH)
}

/**
 * View Holder创建时触发，用于完成视图的初始化
 *
 */
interface ViewHolderCreateListenerForDataBinding<in VH> : Listener<VH> {

    /**
     * @param holder adapter position
     * @param binding viewDataBinding
     */
    fun onCreateViewHolder(holder: VH, binding: ViewDataBinding)
}


/**
 * View Holder更新时触发，用于更新Item数据,ViewBinding专用
 */
interface ViewHolderBindListener<in VH> : Listener<VH> {

    /**
     * @param holder
     * @param item
     */
    fun onBindViewHolder(holder: VH, item: Any, payloads: List<Any>? = mutableListOf())

}


/**
 * View Holder更新时触发，用于更新Item数据,ViewBinding专用
 */
interface ViewHolderBindListenerForViewBinding<in VH> : Listener<VH> {

    /**
     * @param holder
     * @param item
     */
    fun onBindViewHolder(holder: VH, binding: ViewBinding, item: Any, payloads: List<Any>? = mutableListOf())
}

/**
 * View Holder更新时触发，用于更新Item数据,ViewDataBinding专用
 */
interface ViewHolderBindListenerForDataBinding<in VH> : Listener<VH> {

    /**
     * @param holder
     * @param binding
     */
    fun onBindViewHolder(
        holder: VH,
        binding: ViewDataBinding,
        payloads: List<Any>? = mutableListOf()
    )
}
