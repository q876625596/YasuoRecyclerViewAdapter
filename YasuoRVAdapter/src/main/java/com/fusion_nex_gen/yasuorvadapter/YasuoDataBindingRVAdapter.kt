package com.fusion_nex_gen.yasuorvadapter

import android.view.ViewGroup
import androidx.core.util.set
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemDataBindingConfig
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoDataBindingVH
import kotlin.reflect.KClass

/******使用ViewDataBinding  Using ViewDataBinding******/

/**
 * 快速获取已绑定的[YasuoDataBindingRVAdapter]
 * Quickly obtain the bound [YasuoDataBindingRVAdapter]
 */
fun <RV : RecyclerView> RV.getDataBindingAdapter(): YasuoDataBindingRVAdapter {
    return this.adapter as YasuoDataBindingRVAdapter
}

/**
 * 绑定adapter
 * Binding adapter
 * @param life LifecycleOwner object
 * @param itemList [YasuoBaseRVAdapter.itemList]
 * @param headerList [YasuoBaseRVAdapter.headerList]
 * @param footerList [YasuoBaseRVAdapter.footerList]
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterDataBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerList: YasuoList<Any> = YasuoList(),
    footerList: YasuoList<Any> = YasuoList(),
    rvListener: YasuoDataBindingRVAdapter.() -> YasuoDataBindingRVAdapter
): YasuoDataBindingRVAdapter {
    return YasuoDataBindingRVAdapter(life, itemList, headerList, footerList).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * Binding adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterDataBinding(
    adapter: YasuoDataBindingRVAdapter,
    rvListener: YasuoDataBindingRVAdapter.() -> YasuoDataBindingRVAdapter
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoDataBindingRVAdapter(
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseRVAdapter<YasuoDataBindingVH<ViewDataBinding>, YasuoItemDataBindingConfig<YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>>(
    itemList,
    headerItemList,
    footerItemList,
), LifecycleObserver {

    init {
        this.itemList.addOnListChangedCallback(itemListListener)
        this.emptyList.addOnListChangedCallback(emptyListListener)
        this.headerList.addOnListChangedCallback(headerListListener)
        this.footerList.addOnListChangedCallback(footerListListener)
        this.loadMoreList.addOnListChangedCallback(loadMoreListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
        this.emptyList.removeOnListChangedCallback(emptyListListener)
        this.headerList.removeOnListChangedCallback(headerListListener)
        this.footerList.removeOnListChangedCallback(footerListListener)
        this.loadMoreList.removeOnListChangedCallback(loadMoreListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     * Binding life cycle, which must be called after initializing adapter
     */
    fun bindLife(): YasuoDataBindingRVAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoDataBindingVH<ViewDataBinding> {
        initInflater(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater!!, viewType, parent, false)
        val holder = YasuoDataBindingVH(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding): Boolean = let {
                recyclerView?.isComputingLayout?:false
            }

            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView?.isComputingLayout == true) {
                    return
                }
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, dataInvalidation)
                }
            }
        })
        itemIdTypes[viewType]?.holderCreateListener?.invoke(binding, holder)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, position: Int) {
        val item = getItem(position)
        val itemType = itemClassTypes[item::class]
            ?: throw RuntimeException("找不到相应类型的布局，请检查是否绑定布局，position = ${position}\nThe corresponding type of layout cannot be found, please check whether the layout is bound,position = $position")
        holder.binding.setVariable(itemType.variableId, item)
        itemType.holderBindListener?.invoke(holder.binding, holder)
        holder.binding.lifecycleOwner = life
        holder.binding.executePendingBindings()
    }
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * Establish the matching relationship between data class and layout file
 * @param itemLayoutId itemView布局id
 * itemLayoutId
 * @param itemClass 对应实体类的Class
 * Class corresponding to entity class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * Layout corresponding [ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 * Subsequent operations on [YasuoItemDataBindingConfig]
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoDataBindingRVAdapter> Adapter.holderConfig(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    execute?.invoke(itemType)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    return this
}