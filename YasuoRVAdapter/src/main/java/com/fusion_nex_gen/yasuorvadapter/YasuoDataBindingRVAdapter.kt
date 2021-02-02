package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.ViewGroup
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

/******使用viewDataBinding******/

/**
 * 绑定adapter
 * @param context Context 对象
 * @param context Context object
 * @param life LifecycleOwner object
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterDataBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    loadMoreItem: Any? = null,
    rvListener: YasuoRVDataBindingAdapter.() -> YasuoRVDataBindingAdapter
): YasuoRVDataBindingAdapter {
    return YasuoRVDataBindingAdapter(context, life, itemList, headerItemList, footerItemList, loadMoreItem).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterDataBinding(
    adapter: YasuoRVDataBindingAdapter,
    rvListener: YasuoRVDataBindingAdapter.() -> YasuoRVDataBindingAdapter
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVDataBindingAdapter(
    context: Context,
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    loadMoreItem: Any? = null,
) : YasuoBaseRVAdapter<Any, YasuoDataBindingVH<ViewDataBinding>, YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>>(
    context,
    itemList,
    headerItemList,
    footerItemList,
    loadMoreItem
), LifecycleObserver {

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        this.itemList.addOnListChangedCallback(itemListListener)
        this.emptyList.addOnListChangedCallback(emptyListListener)
        this.headerList.addOnListChangedCallback(headerListListener)
        this.footerList.addOnListChangedCallback(footerListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
        this.emptyList.removeOnListChangedCallback(emptyListListener)
        this.headerList.removeOnListChangedCallback(headerListListener)
        this.footerList.removeOnListChangedCallback(footerListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     */
    fun bindLife(): YasuoRVDataBindingAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoDataBindingVH<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, parent, false)
        val holder = YasuoDataBindingVH(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding): Boolean = let {
                recyclerView!!.isComputingLayout
            }

            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView!!.isComputingLayout) {
                    return
                }
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, dataInvalidation)
                }
            }
        })
        //执行holder创建时的监听
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
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    if (isAllFold) {
        itemType.isFold = true
    }
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，header
 * 本质上与[YasuoRVDataBindingAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBindHeader(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，footer
 * 本质上与[YasuoRVDataBindingAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBindFooter(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBindLoadMore(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立Empty数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param bindingClass 布局对应的[ViewDataBinding]
 * @param execute 后续对[YasuoItemDataBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBindEmpty(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfig<T, YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfig<Any, YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}