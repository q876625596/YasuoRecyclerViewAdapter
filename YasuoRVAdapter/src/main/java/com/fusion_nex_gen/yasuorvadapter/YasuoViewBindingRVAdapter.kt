package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
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
    rvListener: YasuoRVViewBindingAdapter.() -> YasuoRVViewBindingAdapter
): YasuoRVViewBindingAdapter {
    return YasuoRVViewBindingAdapter(context, life, itemList, headerItemList, footerItemList).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
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
    // isFold: Boolean = false,
) : YasuoBaseRVAdapter<Any, YasuoViewBindingVH, YasuoItemViewBindingConfig<Any, YasuoViewBindingVH,ViewBinding>>(context, itemList, headerItemList, footerItemList),
    LifecycleObserver {

    init {
        this.itemList.addOnListChangedCallback(itemListListener)
        this.headerList.addOnListChangedCallback(headerListListener)
        this.footerList.addOnListChangedCallback(footerListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
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
/*

    //内部holder创建的监听集合
    private val innerHolderCreateListenerMap: SparseArray<YasuoViewBindingVHCreateListener<YasuoViewBindingVH>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<YasuoViewBindingVHBindListener<YasuoViewBindingVH>> =
        SparseArray()

    override fun <L : YasuoVHListener<YasuoViewBindingVH>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as YasuoViewBindingVHCreateListener<YasuoViewBindingVH>)
    }

    override fun <L : YasuoVHListener<YasuoViewBindingVH>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as YasuoViewBindingVHBindListener<YasuoViewBindingVH>)
    }
*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoViewBindingVH {
        val holder = YasuoViewBindingVH(inflater.inflate(viewType, parent, false))
        //执行holder创建时的监听
        val itemConfig = itemIdTypes[viewType]
        val binding = itemConfig?.createBindingFun?.invoke(holder.itemView) ?: throw RuntimeException("未配置viewBinding")
        holder.binding = binding
        itemConfig.createListener?.invoke(binding, holder)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoViewBindingVH, position: Int) {
        val item = getItem(position)
        itemIdTypes[holder.itemViewType]?.bindListener?.invoke(holder.binding, holder, item)
    }
}

fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> VB,
    block: (YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[kClass] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    block?.invoke(itemType)
    return this
}

fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> VB,
    block: YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>.() -> Unit
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    val itemType = YasuoItemViewBindingConfig<T, YasuoViewBindingVH, VB>(loadMoreLayoutId, createBindingFun = createBindingFun)
    itemClassTypes[loadMoreLayoutItem::class] = itemType as YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[loadMoreLayoutId] = itemType
    itemType.block()
    return this
}


/******下面的几个方法均返回了holder******/

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<T>,
    bindingType: KClass<VB>,
    createListener: ((holder: YasuoViewBindingVH) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoViewBindingVH, item: T, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemClassTypes[kClass] = YasuoBaseItemConfig(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoViewBindingVHCreateListener<YasuoViewBindingVH> {
            override fun onCreateViewHolder(holder: YasuoViewBindingVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoViewBindingVHBindListener<YasuoViewBindingVH> {
            override fun onBindViewHolder(holder: YasuoViewBindingVH, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T, payloads)
            }
        })
    }
    return this
}*/

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> ViewBinding,
    createListener: ((holder: YasuoViewBindingVH) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoViewBindingVH, item: T) -> Unit)? = null
): Adapter {
    itemClassTypes[kClass] = YasuoBaseItemConfig(itemLayoutId, createBindingFun = createBindingFun)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoViewBindingVHCreateListener<YasuoViewBindingVH> {
            override fun onCreateViewHolder(holder: YasuoViewBindingVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoViewBindingVHBindListener<YasuoViewBindingVH> {
            override fun onBindViewHolder(holder: YasuoViewBindingVH, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T)
            }
        })
    }
    return this
}*/

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param loadMoreLayoutId 加载更多布局id
 * @param loadMoreLayoutItem 加载更多布局对应的实体
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
/*
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> ViewBinding,
    createListener: ((holder: YasuoViewBindingVH) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoViewBindingVH, item: T) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemClassTypes[loadMoreLayoutItem::class] = YasuoBaseItemConfig(loadMoreLayoutId, createBindingFun = createBindingFun)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : YasuoViewBindingVHCreateListener<YasuoViewBindingVH> {
            override fun onCreateViewHolder(holder: YasuoViewBindingVH) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : YasuoViewBindingVHBindListener<YasuoViewBindingVH> {
            override fun onBindViewHolder(holder: YasuoViewBindingVH, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T)
            }
        })
    }
    return this
}*/
