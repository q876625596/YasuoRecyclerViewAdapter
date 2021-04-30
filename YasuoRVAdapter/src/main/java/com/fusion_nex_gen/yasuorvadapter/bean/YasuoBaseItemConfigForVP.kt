package com.fusion_nex_gen.yasuorvadapter.bean

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoDataBindingVH
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoNormalVH
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoViewBindingVH

/**
 * 存储一个类型的各种属性
 * Stores various properties of a type
 */
open class YasuoBaseItemConfigForVP<VH : RecyclerView.ViewHolder>(
    //item的布局id
    //The layout ID of the item
    open val itemLayoutId: Int
)

data class YasuoItemDataBindingConfigForVP<VH : YasuoDataBindingVH<VB>, VB : ViewDataBinding>(
    override val itemLayoutId: Int,
    //xml中对应的数据id
    //The corresponding data ID in XML
    var variableId: Int = BR.item,
    //该类型Holder创建时的监听
    //Listen when the type holder is created
    var holderCreateListener: (VB.(holder: YasuoDataBindingVH<VB>) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    //The type of holder is used to listen when binding
    var holderBindListener: (VB.(holder: YasuoDataBindingVH<VB>) -> Unit)? = null,
    //该类型的Holder绑定时的监听，带payloads
    //The type of holder is used to listen when binding, and payloads
    var holderBindAndPayloadsListener: (VB.(holder: YasuoDataBindingVH<VB>, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfigForVP<VH>(itemLayoutId) {
    fun onHolderCreate(createListener: VB.(holder: YasuoDataBindingVH<VB>) -> Unit) {
        this.holderCreateListener = createListener
    }

    fun onHolderBind(bindListener: VB.(holder: YasuoDataBindingVH<VB>) -> Unit) {
        this.holderBindListener = bindListener
    }

    fun onHolderBindAndPayloads(bindAndPayloadsListener: VB.(holder: YasuoDataBindingVH<VB>, payloads: List<Any>?) -> Unit) {
        this.holderBindAndPayloadsListener = bindAndPayloadsListener
    }
}

data class YasuoItemNormalConfigForVP<T : Any, VH : YasuoNormalVH>(
    override val itemLayoutId: Int,
    //该类型Holder创建时的监听
    //Listen when the type holder is created
    var holderCreateListener: ((holder: YasuoNormalVH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    //The type of holder is used to listen when binding
    var holderBindListener: ((holder: YasuoNormalVH, item: T) -> Unit)? = null,
    //该类型的Holder绑定时的监听，带payloads
    //The type of holder is used to listen when binding, and payloads
    var holderBindAndPayloadsListener: ((holder: YasuoNormalVH, item: T, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfigForVP<VH>(itemLayoutId) {
    fun onHolderCreate(createListener: (holder: YasuoNormalVH) -> Unit) {
        this.holderCreateListener = createListener
    }

    fun onHolderBind(bindListener: (holder: YasuoNormalVH, item: T) -> Unit) {
        this.holderBindListener = bindListener
    }

    fun onHolderBindAndPayloads(bindAndPayloadsListener: (holder: YasuoNormalVH, item: T, payloads: List<Any>?) -> Unit) {
        this.holderBindAndPayloadsListener = bindAndPayloadsListener
    }
}

data class YasuoItemViewBindingConfigForVP<T : Any, VH : YasuoViewBindingVH, VB : ViewBinding>(
    override val itemLayoutId: Int,
    //ViewBinding的创建方法
    //How to create ViewBinding
    var createBindingFun: (view: View) -> VB,
    //该类型Holder创建时的监听
    //Listen when the type holder is created
    var holderCreateListener: (VB.(holder: YasuoViewBindingVH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    //The type of holder is used to listen when binding
    var holderBindListener: (VB.(holder: YasuoViewBindingVH, item: T) -> Unit)? = null,
    //该类型的Holder绑定时的监听，带payloads
    //The type of holder is used to listen when binding, and payloads
    var holderBindAndPayloadsListener: (VB.(holder: YasuoViewBindingVH, item: T, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfigForVP<VH>(itemLayoutId) {
    fun onHolderCreate(createListener: VB.(holder: YasuoViewBindingVH) -> Unit) {
        this.holderCreateListener = createListener
    }

    fun onHolderBind(bindListener: VB.(holder: YasuoViewBindingVH, item: T) -> Unit) {
        this.holderBindListener = bindListener
    }

    fun onHolderBindAndPayloads(bindAndPayloadsListener: VB.(holder: YasuoViewBindingVH, item: T, payloads: List<Any>?) -> Unit) {
        this.holderBindAndPayloadsListener = bindAndPayloadsListener
    }
}