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
open class YasuoBaseItemConfig<VH : RecyclerView.ViewHolder>(
    //item的布局id
    //The layout ID of the item
    open val itemLayoutId: Int,
    //该类型布局是否吸顶
    //Is the layout of this type ceiling
    open var sticky: Boolean = false,
    //该类型布局是否支持展开折叠
    //如果是支持折叠的布局，一定要把这个属性设置为true，
    //Whether this type of layout supports expand/fold
    //If the layout supports folding, be sure to set this property to true,
    open val isFold: Boolean = false,
    //该类型布局在grid中的占比
    //为0时不判断该属性
    //The proportion of this type of layout in Grid
    //The property is not judged when 0
    open var gridSpan: Int = 0,
    //该类型布局在staggeredGrid中是否占满
    //Is this type of layout full in the staggeredGrid
    open var staggeredGridFullSpan: Boolean = false,
)

data class YasuoItemDataBindingConfig<VH : YasuoDataBindingVH<VB>, VB : ViewDataBinding>(
    override val itemLayoutId: Int,
    override var sticky: Boolean = false,
    override var isFold: Boolean = false,
    override var gridSpan: Int = 0,
    override var staggeredGridFullSpan: Boolean = false,
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
) : YasuoBaseItemConfig<VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
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

data class YasuoItemNormalConfig<T : Any, VH : YasuoNormalVH>(
    override val itemLayoutId: Int,
    override var sticky: Boolean = false,
    override var isFold: Boolean = false,
    override var gridSpan: Int = 0,
    override var staggeredGridFullSpan: Boolean = false,
    //该类型Holder创建时的监听
    //Listen when the type holder is created
    var holderCreateListener: ((holder: YasuoNormalVH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    //The type of holder is used to listen when binding
    var holderBindListener: ((holder: YasuoNormalVH, item: T) -> Unit)? = null,
    //该类型的Holder绑定时的监听，带payloads
    //The type of holder is used to listen when binding, and payloads
    var holderBindAndPayloadsListener: ((holder: YasuoNormalVH, item: T, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfig<VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
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

data class YasuoItemViewBindingConfig<T : Any, VH : YasuoViewBindingVH, VB : ViewBinding>(
    override val itemLayoutId: Int,
    override var sticky: Boolean = false,
    override var isFold: Boolean = false,
    override var gridSpan: Int = 0,
    override var staggeredGridFullSpan: Boolean = false,
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
) : YasuoBaseItemConfig<VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
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