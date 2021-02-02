package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemViewBindingConfig
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoViewBindingVH
import kotlin.reflect.KClass

/******使用viewBinding******/

/**
 * 绑定adapter
 * @param context Context 对象
 * @param context Context object
 * @param life LifecycleOwner object
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterViewBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    loadMoreItem: Any? = null,
    rvListener: YasuoRVViewBindingAdapter.() -> YasuoRVViewBindingAdapter
): YasuoRVViewBindingAdapter {
    return YasuoRVViewBindingAdapter(context, life, itemList, headerItemList, footerItemList, loadMoreItem).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterViewBinding(
    adapter: YasuoRVViewBindingAdapter,
    rvListener: YasuoRVViewBindingAdapter.() -> YasuoRVViewBindingAdapter
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVViewBindingAdapter(
    context: Context,
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    loadMoreItem: Any? = null,
) : YasuoBaseRVAdapter<Any, YasuoViewBindingVH, YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>>(context, itemList, headerItemList, footerItemList, loadMoreItem),
    LifecycleObserver {

    init {
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
    fun bindLife(): YasuoRVViewBindingAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoViewBindingVH {
        val holder = YasuoViewBindingVH(inflater.inflate(viewType, parent, false))
        //执行holder创建时的监听
        val itemConfig = itemIdTypes[viewType]
        val binding = itemConfig?.createBindingFun?.invoke(holder.itemView) ?: throw RuntimeException("未配置viewBinding")
        holder.binding = binding
        itemConfig.holderCreateListener?.invoke(binding, holder)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoViewBindingVH, position: Int) {
        val item = getItem(position)
        itemIdTypes[holder.itemViewType]?.holderBindListener?.invoke(holder.binding, holder, item)
    }
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: (YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    if (isAllFold) {
        itemType.isFold = true
    }
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，header
 * 本质上与[YasuoRVViewBindingAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindHeader(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: (YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，footer
 * 本质上与[YasuoRVViewBindingAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindFooter(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: (YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindLoadMore(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    itemType.execute()
    return this
}

/**
 * 建立empty数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindEmpty(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    itemType.execute()
    return this
}