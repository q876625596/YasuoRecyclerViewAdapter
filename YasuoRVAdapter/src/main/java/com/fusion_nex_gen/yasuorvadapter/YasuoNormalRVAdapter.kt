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
 * 快速获取已绑定的[YasuoNormalRVAdapter]
 * Quickly obtain the bound [YasuoNormalRVAdapter]
 */
fun <RV : RecyclerView> RV.getNormalAdapter(): YasuoNormalRVAdapter {
    return this.adapter as YasuoNormalRVAdapter
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
inline fun RecyclerView.adapterBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerList: YasuoList<Any> = YasuoList(),
    footerList: YasuoList<Any> = YasuoList(),
    rvListener: YasuoNormalRVAdapter.() -> YasuoNormalRVAdapter
): YasuoNormalRVAdapter {
    return YasuoNormalRVAdapter(life, itemList, headerList, footerList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * Binding adapter
 * @param adapter Adapter实体
 * @param adapter Adapter entity
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
) : YasuoBaseRVAdapter<YasuoNormalVH, YasuoItemNormalConfig<Any, YasuoNormalVH>>(itemList, headerItemList, footerItemList), LifecycleObserver {

    init {
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
     * Binding life cycle, which must be called after initializing adapter
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
 * Establish the matching relationship between data class and layout file
 * @param itemLayoutId itemView布局id
 * itemLayoutId
 * @param itemClass 对应实体类的Class
 * Class corresponding to entity class
 * @param execute 后续对[YasuoItemNormalConfig]的执行操作
 * Subsequent operations on [YasuoItemNormalConfig]
 */
fun <T : Any, Adapter : YasuoNormalRVAdapter> Adapter.holderConfig(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfig<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfig<T, YasuoNormalVH>(itemLayoutId)
    execute?.invoke(itemType)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfig<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    return this
}
