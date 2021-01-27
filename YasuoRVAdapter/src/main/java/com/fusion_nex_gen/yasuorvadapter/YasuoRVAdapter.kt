package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.SparseArray
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.holder.RecyclerViewHolder
import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderBindListener
import com.fusion_nex_gen.yasuorvadapter.interfaces.ViewHolderCreateListener
import kotlin.reflect.KClass

/**
 * 绑定adapter
 * @param context Context 对象
 * @param context Context object
 * @param life LifecycleOwner object
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun <T : Any> RecyclerView.adapterBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: MutableList<T>,
    headerItemList: MutableList<T> = ObList(),
    footerItemList: MutableList<T> = ObList(),
    rvListener: YasuoRVAdapter<T>.() -> YasuoRVAdapter<T>
): YasuoRVAdapter<T> {
    return YasuoRVAdapter(context, life, itemList, headerItemList, footerItemList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun <T : Any> RecyclerView.adapterBinding(
    adapter: YasuoRVAdapter<T>,
    rvListener: YasuoRVAdapter<T>.() -> YasuoRVAdapter<T>
): YasuoRVAdapter<T> {
    return adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVAdapter<T : Any>(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<T> = ObList(),
    headerItemList: MutableList<T> = ObList(),
    footerItemList: MutableList<T> = ObList(),
) : YasuoBaseRVAdapter<T, RecyclerViewHolder>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

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
    fun itemsRemoveListener() {
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
    fun bindLife(): YasuoRVAdapter<T> {
        life.lifecycle.addObserver(this)
        return this
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        life.lifecycle.removeObserver(this)
    }

    //内部holder创建的监听集合
    private val innerHolderCreateListenerMap: SparseArray<ViewHolderCreateListener<RecyclerViewHolder>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListener<RecyclerViewHolder, T>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewHolder>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as ViewHolderCreateListener<RecyclerViewHolder>)
    }

    override fun <L : Listener<RecyclerViewHolder>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as ViewHolderBindListener<RecyclerViewHolder, T>)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val holder = RecyclerViewHolder(inflater.inflate(viewType, parent, false))
        innerHolderCreateListenerMap[viewType]?.onCreateViewHolder(holder)
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
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
 * @param itemLayoutId itemView布局id
 */
inline fun <T : Any, Adapter : YasuoRVAdapter<T>> Adapter.onHolderCreate(
    itemLayoutId: Int,
    crossinline block: (holder: RecyclerViewHolder) -> Unit
): Adapter {
    setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListener<RecyclerViewHolder> {
        override fun onCreateViewHolder(holder: RecyclerViewHolder) {
            block(holder)
        }
    })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bind: (T.(holder: RecyclerViewHolder, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (bind != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListener<RecyclerViewHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: T, payloads: List<Any>?) {
                item.bind(holder, payloads)
            }
        })
    }
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bind: (T.(holder: RecyclerViewHolder) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (bind != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListener<RecyclerViewHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: T, payloads: List<Any>?) {
                item.bind(holder)
            }
        })
    }
    return this
}

/**
 * 建立loadMore数据类与布局文件之间的匹配关系
 * @param loadMoreLayoutId 加载更多布局id
 * @param loadMoreLayoutItem 加载更多布局对应的实体
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    bind: (T.(holder: RecyclerViewHolder) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = ItemType(loadMoreLayoutId)
    if (bind != null) {
        setHolderBindListener(loadMoreLayoutId, object : ViewHolderBindListener<RecyclerViewHolder, T> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: T, payloads: List<Any>?) {
                item.bind(holder)
            }
        })
    }
    return this
}