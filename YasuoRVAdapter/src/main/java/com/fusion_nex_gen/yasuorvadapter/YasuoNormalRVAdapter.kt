package com.fusion_nex_gen.yasuorvadapter

import android.view.ViewGroup
import androidx.core.util.set
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemNormalConfig
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoNormalVH
import kotlin.reflect.KClass

/**
 * 绑定adapter
 * @param context Context 对象
 * @param context Context object
 * @param life LifecycleOwner object
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
    rvListener: YasuoNormalRVAdapter.() -> YasuoNormalRVAdapter
): YasuoNormalRVAdapter {
    return YasuoNormalRVAdapter(life, itemList, headerItemList, footerItemList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterBinding(
    adapter: YasuoNormalRVAdapter,
    rvListener: YasuoNormalRVAdapter.() -> YasuoNormalRVAdapter
): YasuoNormalRVAdapter {
    return adapter.bindLife().rvListener().attach(this)
}

open class YasuoNormalRVAdapter(
    val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseRVAdapter<Any, YasuoNormalVH, YasuoItemNormalConfig<Any, YasuoNormalVH>>(itemList, headerItemList, footerItemList), LifecycleObserver {

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        this.itemList.addOnListChangedCallback(itemListListener)
        this.emptyList.addOnListChangedCallback(emptyListListener)
        this.headerList.addOnListChangedCallback(headerListListener)
        this.footerList.addOnListChangedCallback(footerListListener)
        this.loadMoreList.addOnListChangedCallback(loadMoreListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemsRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
        this.emptyList.removeOnListChangedCallback(emptyListListener)
        this.headerList.removeOnListChangedCallback(headerListListener)
        this.footerList.removeOnListChangedCallback(footerListListener)
        this.loadMoreList.removeOnListChangedCallback(loadMoreListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     */
    fun bindLife(): YasuoNormalRVAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoNormalVH {
        initInflater(parent.context)
        val holder = YasuoNormalVH(inflater!!.inflate(viewType, parent, false))
        itemIdTypes[viewType]?.holderCreateListener?.invoke(holder)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoNormalVH, position: Int) {
        itemIdTypes[holder.itemViewType]?.holderBindListener?.invoke(holder, getItem(position))
    }
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    if (isAllFold) {
        itemType.isFold = true
    }
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，header
 * 本质上与[YasuoNormalRVAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBindHeader(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，footer
 * 本质上与[YasuoNormalRVAdapter.holderBind]没有区别，只是做一下名称上的区分
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBindFooter(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBindLoadMore(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}

/**
 * 建立emptyLayout数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass 对应实体类的Class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderBindEmpty(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    execute?.invoke(itemType)
    return this
}