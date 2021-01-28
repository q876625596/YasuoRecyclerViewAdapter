package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoItemType
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
import com.fusion_nex_gen.yasuorvadapter.holder.YasuoDataBindingVH
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoVHListener
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoDataBindingVHBindListener
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoDataBindingVHCreateListener
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
    isFold: Boolean = false,
    rvListener: YasuoRVDataBindingAdapter.() -> YasuoRVDataBindingAdapter
): YasuoRVDataBindingAdapter {
    return YasuoRVDataBindingAdapter(context, life, itemList, headerItemList, footerItemList, isFold).bindLife().rvListener()
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
    isFold: Boolean = false,
) : YasuoBaseRVAdapter<Any, YasuoDataBindingVH<ViewDataBinding>>(context, itemList, headerItemList, footerItemList, isFold), LifecycleObserver {

    /**
     * 如果为true，那么布局中的variableId默认为BR.item，可以提升性能
     */
    var variableIdIsDefault = true

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
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
    fun bindLife(): YasuoRVDataBindingAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    //内部holder创建的监听集合
    private val innerHolderCreateListenerMap: SparseArray<YasuoDataBindingVHCreateListener<YasuoDataBindingVH<ViewDataBinding>>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<YasuoDataBindingVHBindListener<YasuoDataBindingVH<ViewDataBinding>>> =
        SparseArray()

    override fun <L : YasuoVHListener<YasuoDataBindingVH<ViewDataBinding>>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as YasuoDataBindingVHCreateListener<YasuoDataBindingVH<ViewDataBinding>>)
    }

    override fun <L : YasuoVHListener<YasuoDataBindingVH<ViewDataBinding>>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as YasuoDataBindingVHBindListener<YasuoDataBindingVH<ViewDataBinding>>)
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
        innerHolderCreateListenerMap[viewType]?.onCreateViewHolder(holder, binding)
        return holder
    }

    override fun onBindViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, position: Int) {
        val item = getItem(position)
        if (variableIdIsDefault) {
            holder.binding.setVariable(BR.item, item)
        } else {
            //否则去查找自定义variableId
            val itemType = itemTypes[item::class]
                ?: throw RuntimeException("找不到相应类型的布局，请检查是否绑定布局，position = ${position}\nThe corresponding type of layout cannot be found, please check whether the layout is bound,position = $position")
            if (itemType.variableId != null) {
                holder.binding.setVariable(itemType.variableId, item)
            }
        }
        innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, holder.binding)
        holder.binding.lifecycleOwner = life
        holder.binding.executePendingBindings()
    }
}

/******下面的几个方法均返回了holder******/

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindingType ViewDataBinding::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.onHolderDataBindingAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindingType: KClass<VB>,
    customItemBR: Int = BR.item,
    createListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = YasuoItemType(itemLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoDataBindingVHCreateListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoDataBindingVHBindListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onBindViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, payloads)
            }
        })
    }
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass Item::class
 * @param bindingClass ViewDataBinding::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    itemClass: KClass<*>,
    bindingClass: KClass<VB>,
    customItemBR: Int = BR.item,
    createListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>) -> Unit)? = null
): Adapter {
    itemTypes[itemClass] = YasuoItemType(itemLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : YasuoDataBindingVHCreateListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : YasuoDataBindingVHBindListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onBindViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
                (binding as VB).bindListener(holder)
            }
        })
    }
    return this
}

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param loadMoreLayoutId 加载更多布局id
 * @param loadMoreLayoutItem 加载更多布局对应的实体
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bindingClass: KClass<VB>,
    customItemBR: Int = BR.item,
    createListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: YasuoDataBindingVH<ViewDataBinding>) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = YasuoItemType(loadMoreLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : YasuoDataBindingVHCreateListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : YasuoDataBindingVHBindListener<YasuoDataBindingVH<ViewDataBinding>> {
            override fun onBindViewHolder(holder: YasuoDataBindingVH<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
                (binding as VB).bindListener(holder)
            }
        })
    }
    return this
}