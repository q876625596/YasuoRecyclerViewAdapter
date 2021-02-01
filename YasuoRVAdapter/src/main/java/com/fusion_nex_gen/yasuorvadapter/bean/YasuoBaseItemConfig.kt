package com.fusion_nex_gen.yasuorvadapter.bean

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoDataBindingVH
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoNormalVH
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoViewBindingVH

//存储一个类型的各种属性
open class YasuoBaseItemConfig<T : Any, VH : RecyclerView.ViewHolder>(
    //item的布局id
    open val itemLayoutId: Int,
    //该类型布局是否吸顶
    open var sticky: Boolean = false,
    //该类型布局是否支持展开折叠
    open val isFold: Boolean = false,
    //该类型布局在grid中的占比，
    //loadMore，空布局会默认为占满
    open var gridSpan: Int = 1,
    //该类型布局在staggeredGrid中是否占满
    //loadMore，空布局会默认为占满
    open var staggeredGridFullSpan: Boolean = false,
/*    //创建ViewBinding
    val createBindingFun: ((view: View) -> ViewBinding)? = null,
    //该类型Holder创建时的监听
    val createListener: ((holder: VH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    val bindListener: (T.(holder: VH) -> Unit)? = null,
    ////该类型的Holder绑定时的监听，带payloads
    val bindAndPayloadsListener: (T.(holder: VH, payloads: List<Any>?) -> Unit)? = null*/
)

//存储一个类型的各种属性
data class YasuoItemDataBindingConfig<T : Any, VH : YasuoDataBindingVH<VB>, VB : ViewDataBinding>(
    //item的布局id
    override val itemLayoutId: Int,
    //该类型布局是否吸顶
    override var sticky: Boolean = false,
    //该类型布局是否支持展开折叠
    override var isFold: Boolean = false,
    //该类型布局在grid中的占比，
    //loadMore，空布局会默认为占满
    override var gridSpan: Int = 1,
    //该类型布局在staggeredGrid中是否占满
    //loadMore，空布局会默认为占满
    override var staggeredGridFullSpan: Boolean = false,
    var variableId: Int = BR.item,
    //该类型Holder创建时的监听
    val createListener: (VB.(holder: YasuoDataBindingVH<VB>) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    var bindListener: (VB.(holder: YasuoDataBindingVH<VB>) -> Unit)? = null,
    ////该类型的Holder绑定时的监听，带payloads
    var bindAndPayloadsListener: (VB.(holder: YasuoDataBindingVH<VB>, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfig<T, VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
    fun onBind(bindListener: VB.(holder: YasuoDataBindingVH<VB>) -> Unit) {
        this.bindListener = bindListener
    }
}

//存储一个类型的各种属性
data class YasuoItemNormalConfig<T : Any, VH : YasuoNormalVH>(
    //item的布局id
    override val itemLayoutId: Int,
    //该类型布局是否吸顶
    override var sticky: Boolean = false,
    //该类型布局是否支持展开折叠
    override var isFold: Boolean = false,
    //该类型布局在grid中的占比，
    //loadMore，空布局会默认为占满
    override var gridSpan: Int = 1,
    //该类型布局在staggeredGrid中是否占满
    //loadMore，空布局会默认为占满
    override var staggeredGridFullSpan: Boolean = false,
    //该类型Holder创建时的监听
    val createListener: ((holder: YasuoNormalVH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    var bindListener: ((holder: YasuoNormalVH, item: T) -> Unit)? = null,
    ////该类型的Holder绑定时的监听，带payloads
    var bindAndPayloadsListener: (T.(holder: YasuoNormalVH, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfig<T, VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
    fun onBind(bindListener: (holder: YasuoNormalVH, item: T) -> Unit) {
        this.bindListener = bindListener
    }
}

data class YasuoItemViewBindingConfig<T : Any, VH : YasuoViewBindingVH, VB : ViewBinding>(
    //item的布局id
    override val itemLayoutId: Int,
    //该类型布局是否吸顶
    override var sticky: Boolean = false,
    //该类型布局是否支持展开折叠
    //如果是支持折叠的布局，一定要把这个属性设置为true，
    override var isFold: Boolean = false,
    //该类型布局在grid中的占比，
    //loadMore，空布局会默认为占满
    override var gridSpan: Int = 1,
    //该类型布局在staggeredGrid中是否占满
    //loadMore，空布局会默认为占满
    override var staggeredGridFullSpan: Boolean = false,
    var createBindingFun: (view: View) -> VB,
    //该类型Holder创建时的监听
    var createListener: (VB.(holder: YasuoViewBindingVH) -> Unit)? = null,
    //该类型的Holder绑定时的监听
    var bindListener: (VB.(holder: YasuoViewBindingVH, item: T) -> Unit)? = null,
    ////该类型的Holder绑定时的监听，带payloads
    var bindAndPayloadsListener: (VB.(holder: YasuoViewBindingVH, item: T, payloads: List<Any>?) -> Unit)? = null
) : YasuoBaseItemConfig<T, VH>(itemLayoutId, sticky, isFold, gridSpan, staggeredGridFullSpan) {
    fun onBind(bindListener: VB.(holder: YasuoViewBindingVH, item: T) -> Unit) {
        this.bindListener = bindListener
    }
}