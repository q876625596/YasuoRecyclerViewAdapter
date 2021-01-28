package com.fusion_nex_gen.yasuorvadapter.listener

import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding


interface YasuoVHListener<in VH>

/**
 * View Holder创建时触发，用于完成视图的初始化
 *
 */
interface YasuoVHCreateListener<in VH> : YasuoVHListener<VH> {
    /**
     * @param holder adapter position
     */
    fun onCreateViewHolder(holder: VH)

}

/**
 * View Holder创建时触发，用于完成视图的初始化
 *
 */
interface YasuoViewBindingVHCreateListener<in VH> : YasuoVHListener<VH> {

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
interface YasuoDataBindingVHCreateListener<in VH> : YasuoVHListener<VH> {

    /**
     * @param holder adapter position
     * @param binding viewDataBinding
     */
    fun onCreateViewHolder(holder: VH, binding: ViewDataBinding)
}


/**
 * View Holder更新时触发，用于更新Item数据
 */
interface YasuoVHBindListener<in VH> : YasuoVHListener<VH> {

    /**
     * @param holder
     * @param item
     */
    fun onBindViewHolder(holder: VH, item: Any, payloads: List<Any>? = mutableListOf())

}


/**
 * View Holder更新时触发，用于更新Item数据,ViewBinding专用
 */
interface YasuoViewBindingVHBindListener<in VH> : YasuoVHListener<VH> {

    /**
     * @param holder
     * @param item
     */
    fun onBindViewHolder(holder: VH, binding: ViewBinding, item: Any, payloads: List<Any>? = mutableListOf())
}

/**
 * View Holder更新时触发，用于更新Item数据,ViewDataBinding专用
 */
interface YasuoDataBindingVHBindListener<in VH> : YasuoVHListener<VH> {

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
