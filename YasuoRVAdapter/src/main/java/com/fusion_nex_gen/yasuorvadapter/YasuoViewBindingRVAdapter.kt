package com.fusion_nex_gen.yasuorvadapter

import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
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

/******使用ViewBinding  Using ViewBinding******/

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
inline fun RecyclerView.adapterViewBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    headerList: YasuoList<Any> = YasuoList(),
    footerList: YasuoList<Any> = YasuoList(),
    rvListener: YasuoRVViewBindingAdapter.() -> YasuoRVViewBindingAdapter
): YasuoRVViewBindingAdapter {
    return YasuoRVViewBindingAdapter(life, itemList, headerList, footerList).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * Binding adapter
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
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
    headerItemList: YasuoList<Any> = YasuoList(),
    footerItemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseRVAdapter<Any, YasuoViewBindingVH, YasuoItemViewBindingConfig<Any, YasuoViewBindingVH, ViewBinding>>(itemList, headerItemList, footerItemList),
    LifecycleObserver {

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
    fun bindLife(): YasuoRVViewBindingAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YasuoViewBindingVH {
        initInflater(parent.context)
        val holder = YasuoViewBindingVH(inflater!!.inflate(viewType, parent, false))
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
 * Establish the matching relationship between data class and layout file
 * @param itemLayoutId itemView布局id
 * itemLayoutId
 * @param itemClass 对应实体类的Class
 * Class corresponding to entity class
 * @param createBindingFun 用于在[YasuoRVViewBindingAdapter.onCreateViewHolder]中创建[ViewBinding]
 * used in [YasuoRVViewBindingAdapter.onCreateViewHolder]Create [ViewBinding] in
 * @param execute 后续对[YasuoItemViewBindingConfig]的执行操作
 * Subsequent operations on [YasuoItemViewBindingConfig]
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderConfig(
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