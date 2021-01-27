package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.fusion_nex_gen.yasuorvadapter.holder.RecyclerViewBindingHolder
import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderBindListenerForViewBinding
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderCreateListenerForViewBinding
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
    itemList: MutableList<Any>,
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
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
    itemList: MutableList<Any> = ObList(),
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
) : YasuoBaseRVAdapter<Any, RecyclerViewBindingHolder>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

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
    fun bindLife(): YasuoRVViewBindingAdapter {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    //内部holder创建的监听集合
    private val innerHolderCreateListenerMap: SparseArray<ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewBindingHolder>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder>)
    }

    override fun <L : Listener<RecyclerViewBindingHolder>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewBindingHolder {
        val holder = RecyclerViewBindingHolder(inflater.inflate(viewType, parent, false))
        //执行holder创建时的监听
        innerHolderCreateListenerMap[viewType]?.onCreateViewHolder(holder)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerViewBindingHolder, position: Int) {
        //非禁用全局监听的布局才执行
        if (!disableGlobalItemHolderListenerType(holder.itemViewType)) {
            //执行之前判断非空
            getGlobalItemHolderListener()?.invoke(holder)
        }

        val item = when {
            isEmptyLayoutMode() -> emptyLayoutItem!!
            inAllList(position) -> getItem(position)
            hasLoadMore() -> loadMoreLayoutItem!!
            else -> throw RuntimeException("onBindViewHolder position error! position = $position")
        }
        if (!holder.isInitBinding()) {
            holder.createBinding {
                itemTypes[item::class]?.createBindingFun?.invoke(it)
                    ?: throw RuntimeException("The method to create ViewBinding is not set")
            }
        }
        innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, holder.binding!!, item)
    }
}


/******下面的几个方法均返回了holder******/

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<T>,
    bindingType: KClass<VB>,
    createListener: ((holder: RecyclerViewBindingHolder) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerViewBindingHolder, item: T, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewBindingHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T, payloads)
            }
        })
    }
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> ViewBinding,
    createListener: ((holder: RecyclerViewBindingHolder) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerViewBindingHolder, item: T) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId, createBindingFun = createBindingFun)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewBindingHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T)
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
fun <T : Any, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bindingType: KClass<VB>,
    createBindingFun: (view: View) -> ViewBinding,
    createListener: ((holder: RecyclerViewBindingHolder) -> Unit)? = null,
    bindListener: (VB.(holder: RecyclerViewBindingHolder, item: T) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = ItemType(loadMoreLayoutId, createBindingFun = createBindingFun)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewBindingHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, binding: ViewBinding, item: Any, payloads: List<Any>?) {
                (binding as VB).bindListener(holder, item as T)
            }
        })
    }
    return this
}