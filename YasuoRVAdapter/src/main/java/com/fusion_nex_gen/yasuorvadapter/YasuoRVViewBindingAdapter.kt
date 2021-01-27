package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.SparseArray
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
inline fun <T : Any> RecyclerView.adapterViewBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: MutableList<T>,
    headerItemList: MutableList<T> = ObList(),
    footerItemList: MutableList<T> = ObList(),
    rvListener: YasuoRVViewBindingAdapter<T>.() -> YasuoRVViewBindingAdapter<T>
): YasuoRVViewBindingAdapter<T> {
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
inline fun <T : Any> RecyclerView.adapterViewBinding(
    adapter: YasuoRVViewBindingAdapter<T>,
    rvListener: YasuoRVViewBindingAdapter<T>.() -> YasuoRVViewBindingAdapter<T>
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVViewBindingAdapter<T : Any>(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<T> = ObList(),
    headerItemList: MutableList<T> = ObList(),
    footerItemList: MutableList<T> = ObList(),
) : YasuoBaseRVAdapter<T, RecyclerViewBindingHolder>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        if (this.itemList is ObList<T>) {
            (this.itemList as ObList<T>).addOnListChangedCallback(itemListListener)
        }
        if (this.headerItemList is ObList<T>) {
            (this.headerItemList as ObList<T>).addOnListChangedCallback(headerListListener)
        }
        if (this.footerItemList is ObList<T>) {
            (this.footerItemList as ObList<T>).addOnListChangedCallback(footerListListener)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemListRemoveListener() {
        if (this.itemList is ObList<T>) {
            (this.itemList as ObList<T>).removeOnListChangedCallback(itemListListener)
        }
        if (this.headerItemList is ObList<T>) {
            (this.headerItemList as ObList<T>).removeOnListChangedCallback(headerListListener)
        }
        if (this.footerItemList is ObList<T>) {
            (this.footerItemList as ObList<T>).removeOnListChangedCallback(footerListListener)
        }
    }

    /**
     * 绑定生命周期，初始化adapter之后必须调用
     */
    fun bindLife(): YasuoRVViewBindingAdapter<T> {
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
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder, T>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewBindingHolder>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder>)
    }

    override fun <L : Listener<RecyclerViewBindingHolder>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder, T>)
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
        when {
            isEmptyLayoutMode() -> innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, emptyLayoutItem!!)
            inAllList(position) -> innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, getItem(position))
            hasLoadMore() -> innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder, loadMoreLayoutItem!!)
            else -> throw RuntimeException("onBindViewHolder position error! position = $position")
        }
    }
}


/**
 * View Holder创建时触发
 */
inline fun <T : Any, Adapter : YasuoRVViewBindingAdapter<T>> Adapter.onHolderCreate(
    itemLayoutId: Int,
    crossinline createListener: (holder: RecyclerViewBindingHolder) -> Unit
): Adapter {
    setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder> {
        override fun onCreateViewHolder(holder: RecyclerViewBindingHolder) {
            createListener(holder)
        }
    })
    return this
}

/*ViewBinding*/

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVViewBindingAdapter<T>> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindListener: (T.(holder: RecyclerViewBindingHolder, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, item: T, payloads: List<Any>?) {
                item.bindListener(holder, payloads)
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
fun <T : Any, Adapter : YasuoRVViewBindingAdapter<T>> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindListener: (T.(holder: RecyclerViewBindingHolder) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, item: T, payloads: List<Any>?) {
                item.bindListener(holder)
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
fun <T : Any, Adapter : YasuoRVViewBindingAdapter<T>> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bindListener: (T.(holder: RecyclerViewBindingHolder) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = ItemType(loadMoreLayoutId)
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewBindingHolder, item: T, payloads: List<Any>?) {
                item.bindListener(holder)
            }
        })
    }
    return this
}
