package com.fusion_nex_gen.yasuorvadapter.viewPager

import android.view.View
import android.view.ViewGroup
import androidx.core.util.set
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemViewBindingConfigForVP
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoViewBindingVH
import kotlin.reflect.KClass

/******使用ViewBinding  Using ViewBinding******/

/**
 * 绑定adapter
 * Binding adapter
 * @param life LifecycleOwner object
 * @param itemList [YasuoBaseVPAdapter.itemList]
 * @param vpListener 绑定Adapter实体之前需要做的操作
 * @param vpListener What to do before binding adapter entity
 */
inline fun ViewPager2.adapterViewBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    vpListener: YasuoViewBindingVPAdapter.() -> YasuoViewBindingVPAdapter
): YasuoViewBindingVPAdapter {
    return YasuoViewBindingVPAdapter(life, itemList).bindLife().vpListener()
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
inline fun ViewPager2.adapterViewBinding(
    adapter: YasuoViewBindingVPAdapter,
    rvListener: YasuoViewBindingVPAdapter.() -> YasuoViewBindingVPAdapter
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoViewBindingVPAdapter(
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseVPAdapter<YasuoViewBindingVH, YasuoItemViewBindingConfigForVP<Any, YasuoViewBindingVH, ViewBinding>>(itemList),
    LifecycleObserver {

    init {
        this.itemList.addOnListChangedCallback(itemListListener)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        this.itemList.removeOnListChangedCallback(itemListListener)
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     * Binding life cycle, which must be called after initializing adapter
     */
    fun bindLife(): YasuoViewBindingVPAdapter {
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
 * @param createBindingFun 用于在[YasuoViewBindingVPAdapter.onCreateViewHolder]中创建[ViewBinding]
 * used in [YasuoViewBindingVPAdapter.onCreateViewHolder]Create [ViewBinding] in
 * @param execute 后续对[YasuoItemViewBindingConfigForVP]的执行操作
 * Subsequent operations on [YasuoItemViewBindingConfigForVP]
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoViewBindingVPAdapter> Adapter.holderConfig(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    createBindingFun: (view: View) -> VB,
    execute: (YasuoItemViewBindingConfigForVP<T, YasuoViewBindingVH, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemViewBindingConfigForVP<T, YasuoViewBindingVH, VB>(itemLayoutId, createBindingFun = createBindingFun)
    execute?.invoke(itemType)
    itemClassTypes[itemClass] = itemType as YasuoItemViewBindingConfigForVP<Any, YasuoViewBindingVH, ViewBinding>
    itemIdTypes[itemLayoutId] = itemType
    return this
}