package com.fusion_nex_gen.yasuorvadapter.viewPager

import android.view.ViewGroup
import androidx.core.util.set
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemDataBindingConfigForVP
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoDataBindingVH
import kotlin.reflect.KClass

/******使用ViewDataBinding  Using ViewDataBinding******/

/**
 * 绑定adapter
 * Binding adapter
 * @param life LifecycleOwner object
 * @param itemList [YasuoBaseVPAdapter.itemList]
 * @param vpListener 绑定Adapter实体之前需要做的操作
 * @param vpListener What to do before binding adapter entity
 */
inline fun ViewPager2.adapterDataBinding(
    life: LifecycleOwner,
    itemList: YasuoList<Any>,
    vpListener: YasuoVPDataBindingAdapter.() -> YasuoVPDataBindingAdapter
): YasuoVPDataBindingAdapter {
    return YasuoVPDataBindingAdapter(life, itemList).bindLife().vpListener()
        .attach(this)
}

/**
 * 绑定adapter
 * Binding adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param vpListener 绑定Adapter实体之前需要做的操作
 * @param vpListener What to do before binding adapter entity
 */
inline fun ViewPager2.adapterDataBinding(
    adapter: YasuoVPDataBindingAdapter,
    vpListener: YasuoVPDataBindingAdapter.() -> YasuoVPDataBindingAdapter
) {
    adapter.bindLife().vpListener().attach(this)
}

open class YasuoVPDataBindingAdapter(
    private val life: LifecycleOwner,
    itemList: YasuoList<Any> = YasuoList(),
) : YasuoBaseVPAdapter<YasuoDataBindingVH<ViewDataBinding>, YasuoItemDataBindingConfigForVP<YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>>(
    itemList,
), LifecycleObserver {

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
    fun bindLife(): YasuoVPDataBindingAdapter {
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
 * @param execute 后续对[YasuoItemDataBindingConfigForVP]的执行操作
 * Subsequent operations on [YasuoItemDataBindingConfigForVP]
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoVPDataBindingAdapter> Adapter.holderConfigVP(
    itemLayoutId: Int,
    itemClass: KClass<T>,
    bindingClass: KClass<VB>,
    execute: (YasuoItemDataBindingConfigForVP<YasuoDataBindingVH<VB>, VB>.() -> Unit)? = null
): Adapter {
    val itemType = YasuoItemDataBindingConfigForVP<YasuoDataBindingVH<VB>, VB>(itemLayoutId)
    execute?.invoke(itemType)
    itemClassTypes[itemClass] = itemType as YasuoItemDataBindingConfigForVP<YasuoDataBindingVH<ViewDataBinding>, ViewDataBinding>
    itemIdTypes[itemLayoutId] = itemType
    return this
}