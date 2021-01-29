package com.fusion_nex_gen.yasuorvadapter.listener

import android.view.View
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


/**
 * 监听Sticky header item view的创建
 */
interface StickyViewHolderCreateListener<in VH> : YasuoVHListener<VH> {
    /**
     * @param holder
     */
    fun onCreateHeaderViewHolder(holder: VH)
}

/**
 * 监听Sticky header item view的绑定
 */
interface StickyViewHolderBindListener<in VH> : YasuoVHListener<VH> {
    /**
     * @param holder
     * @param position
     */
    fun onBindHeaderViewHolder(holder: VH, position: Int)
}

/**
 * 监听Sticky header item view的点击事件
 */
interface StickyClickListener<in VH> : YasuoVHListener<VH> {
    /**
     * @param holder
     * @param clickView
     * @param position
     */
    fun onHeaderClick(holder: VH, clickView: View, position: Int)
}
