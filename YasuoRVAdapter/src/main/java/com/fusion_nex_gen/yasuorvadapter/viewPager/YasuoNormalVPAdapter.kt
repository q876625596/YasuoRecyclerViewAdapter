package com.fusion_nex_gen.yasuorvadapter.viewPager

import android.view.ViewGroup
import androidx.core.util.set
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemNormalConfigForVP
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoNormalVH
import kotlin.reflect.KClass

/**
 * 绑定adapter
 * Binding adapter
 * @param life LifecycleOwner object
 * @param itemList [YasuoBaseVPAdapter.itemList]
 * @param headerList [YasuoBaseVPAdapter.headerList]
 * @param footerList [YasuoBaseVPAdapter.footerList]
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun ViewPager2.adapterBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerList: YasuoList<Any> = YasuoList(),
    footerList: YasuoList<Any> = YasuoList(),
    rvListener: YasuoNormalVPAdapter.() -> YasuoNormalVPAdapter
): YasuoNormalVPAdapter {
    return YasuoNormalVPAdapter(life, itemList, headerList, footerList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * Binding adapter
 * @param adapter Adapter实体
 * @param adapter Adapter entity
 * @param vpListener 绑定Adapter实体之前需要做的操作
 * @param vpListener What to do before binding adapter entity
 */
inline fun ViewPager2.adapterBinding(
    adapter: YasuoNormalVPAdapter,
    vpListener: YasuoNormalVPAdapter.() -> YasuoNormalVPAdapter
): YasuoNormalVPAdapter {
    return adapter.bindLife().vpListener().attach(this)
}

open class YasuoNormalVPAdapter(
    val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseVPAdapter<YasuoNormalVH, YasuoItemNormalConfigForVP<Any, YasuoNormalVH>>(itemList), LifecycleObserver {

    init {
        this.itemList.addOnListChangedCallback(itemListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemsRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     * Binding life cycle, which must be called after initializing adapter
     */
    fun bindLife(): YasuoNormalVPAdapter {
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
 * @param execute 后续对[YasuoItemNormalConfigForVP]的执行操作
 * Subsequent operations on [YasuoItemNormalConfigForVP]
 */
fun <T : Any, Adapter : YasuoNormalVPAdapter> Adapter.holderConfigVP(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    execute: (YasuoItemNormalConfigForVP<T, YasuoNormalVH>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemNormalConfigForVP<T, YasuoNormalVH>(itemLayoutId)
    execute?.invoke(itemType)
    itemClassTypes[itemClass] = itemType as YasuoItemNormalConfigForVP<Any, YasuoNormalVH>
    itemIdTypes[itemLayoutId] = itemType
    return this
}
