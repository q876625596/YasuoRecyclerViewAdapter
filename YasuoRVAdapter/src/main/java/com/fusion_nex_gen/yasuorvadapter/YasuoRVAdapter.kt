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
inline fun RecyclerView.adapterBinding(
    context: Context,
    life: LifecycleOwner,
    itemList: MutableList<Any>,
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
    rvListener: YasuoRVAdapter.() -> YasuoRVAdapter
): YasuoRVAdapter {
    return YasuoRVAdapter(context, life, itemList, headerItemList, footerItemList).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun RecyclerView.adapterBinding(
    adapter: YasuoRVAdapter,
    rvListener: YasuoRVAdapter.() -> YasuoRVAdapter
): YasuoRVAdapter {
    return adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVAdapter(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<Any> = ObList(),
    headerItemList: MutableList<Any> = ObList(),
    footerItemList: MutableList<Any> = ObList(),
) : YasuoBaseRVAdapter<Any, RecyclerViewHolder>(context, itemList, headerItemList, footerItemList), LifecycleObserver {

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
    fun itemsRemoveListener() {
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
    fun bindLife(): YasuoRVAdapter {
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
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListener<RecyclerViewHolder>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewHolder>> setHolderCreateListener(type: Int, listener: L) {
        innerHolderCreateListenerMap.put(type, listener as ViewHolderCreateListener<RecyclerViewHolder>)
    }

    override fun <L : Listener<RecyclerViewHolder>> setHolderBindListener(type: Int, listener: L) {
        innerHolderBindListenerMap.put(type, listener as ViewHolderBindListener<RecyclerViewHolder>)
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
        innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(
            holder, when {
                isEmptyLayoutMode() -> emptyLayoutItem!!
                inAllList(position) -> getItem(position)
                hasLoadMore() -> loadMoreLayoutItem!!
                else -> throw RuntimeException("onBindViewHolder position error! position = $position")
            }
        )

    }
}

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter> Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<T>,
    createListener: ((holder: RecyclerViewHolder) -> Unit)? = null,
    bindListener: (T.(holder: RecyclerViewHolder, payloads: List<Any>?) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListener<RecyclerViewHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListener<RecyclerViewHolder> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder, payloads)
            }
        })
    }
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass 实体类::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter> Adapter.holderBind(
    itemLayoutId: Int,
    kClass: KClass<T>,
    createListener: ((holder: RecyclerViewHolder) -> Unit)? = null,
    bindListener: (T.(holder: RecyclerViewHolder) -> Unit)? = null
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    if (createListener != null) {
        setHolderCreateListener(itemLayoutId, object : ViewHolderCreateListener<RecyclerViewHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(itemLayoutId, object : ViewHolderBindListener<RecyclerViewHolder> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder)
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
fun <T : Any, Adapter : YasuoRVAdapter> Adapter.holderBindLoadMore(
    loadMoreLayoutId: Int,
    loadMoreLayoutItem: T,
    createListener: ((holder: RecyclerViewHolder) -> Unit)? = null,
    bindListener: (T.(holder: RecyclerViewHolder) -> Unit)? = null
): Adapter {
    this.loadMoreLayoutId = loadMoreLayoutId
    this.loadMoreLayoutItem = loadMoreLayoutItem
    itemTypes[loadMoreLayoutItem::class] = ItemType(loadMoreLayoutId)
    if (createListener != null) {
        setHolderCreateListener(loadMoreLayoutId, object : ViewHolderCreateListener<RecyclerViewHolder> {
            override fun onCreateViewHolder(holder: RecyclerViewHolder) {
                createListener(holder)
            }
        })
    }
    if (bindListener != null) {
        setHolderBindListener(loadMoreLayoutId, object : ViewHolderBindListener<RecyclerViewHolder> {
            override fun onBindViewHolder(holder: RecyclerViewHolder, item: Any, payloads: List<Any>?) {
                (item as T).bindListener(holder)
            }
        })
    }
    return this
}