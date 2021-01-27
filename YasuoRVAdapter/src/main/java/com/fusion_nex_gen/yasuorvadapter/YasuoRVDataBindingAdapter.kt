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
import com.fusion_nex_gen.yasuorvadapter.holder.RecyclerDataBindingHolder
import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderBindListenerForDataBinding
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderCreateListenerForDataBinding
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
    itemList: MutableList<Any>,
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
    rvListener: YasuoRVDataBindingAdapter.() -> YasuoRVDataBindingAdapter
): YasuoRVDataBindingAdapter {
    return YasuoRVDataBindingAdapter(context, life, itemList, headerItemList, footerItemList).bindLife().rvListener()
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
    itemList: MutableList<Any> = ObList(),
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
) : YasuoBaseRVAdapter<Any, RecyclerDataBindingHolder<ViewDataBinding>>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

    /**
     * 如果为true，那么布局中的variableId默认为BR.item，可以提升性能
     */
    var variableIdIsDefault = true

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        if (this.itemList is ObList<Any>) {
            (this.itemList as ObList<Any>).addOnListChangedCallback(itemListListener)
        }
        if (this.headerItemList is ObList<Any>) {
            (this.headerItemList as ObList<Any>).addOnListChangedCallback(headerListListener)
        }
        if (this.footerItemList is ObList<Any>) {
            (this.footerItemList as ObList<Any>).addOnListChangedCallback(footerListListener)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        if (this.itemList is ObList<Any>) {
            (this.itemList as ObList<Any>).removeOnListChangedCallback(itemListListener)
        }
        if (this.headerItemList is ObList<Any>) {
            (this.headerItemList as ObList<Any>).removeOnListChangedCallback(headerListListener)
        }
        if (this.footerItemList is ObList<Any>) {
            (this.footerItemList as ObList<Any>).removeOnListChangedCallback(footerListListener)
        }
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
    private val innerHolderCreateListenerMap: SparseArray<ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>> =
        SparseArray()

    override fun <L : Listener<RecyclerDataBindingHolder<ViewDataBinding>>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>)
    }

    override fun <L : Listener<RecyclerDataBindingHolder<ViewDataBinding>>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerDataBindingHolder<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, parent, false)
        val holder = RecyclerDataBindingHolder(binding)
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

    override fun onBindViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, position: Int) {
        //非禁用全局监听的布局才执行
        if (!disableGlobalItemHolderListenerType(holder.itemViewType)) {
            //执行之前判断非空
            getGlobalItemHolderListener()?.invoke(holder)
        }
        when {
            //判断是全屏布局
            isEmptyLayoutMode() -> holder.binding.setVariable(BR.item, emptyLayoutItem)
            //普通item
            inAllList(position) -> {
                //如果使用默认variableId
                if (variableIdIsDefault) {
                    holder.binding.setVariable(BR.item, getItem(position))
                } else {
                    //否则去查找自定义variableId
                    val item = getItem(position)
                    val itemType = itemTypes[item::class]
                    if (itemType != null) {
                        if (itemType.variableId != null) {
                            holder.binding.setVariable(itemType.variableId, item)
                        }
                    } else {
                        throw Exception("A layout of the corresponding type was not found")
                    }
                }
            }
            //loadMoreView
            hasLoadMore() -> holder.binding.setVariable(BR.item, loadMoreLayoutItem)
            else -> throw RuntimeException("onBindViewHolder position error! position = $position")
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
    createListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onBindViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
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
    createListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit)? = null
): Adapter {
    itemTypes[itemClass] = ItemType(itemLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onBindViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
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
    createListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = ItemType(loadMoreLayoutId, customItemBR)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onCreateViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding) {
                (binding as VB).createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onBindViewHolder(holder: RecyclerDataBindingHolder<ViewDataBinding>, binding: ViewDataBinding, payloads: List<Any>?) {
                (binding as VB).bindListener(holder)
            }
        })
    }
    return this
}