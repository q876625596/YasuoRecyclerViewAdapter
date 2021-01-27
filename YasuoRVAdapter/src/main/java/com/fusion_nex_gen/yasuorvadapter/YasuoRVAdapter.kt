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
inline fun <T : Any> RecyclerView.rvAdapter(
    context: Context,
    life: LifecycleOwner,
    list: MutableList<T>,
    rvListener: YasuoRVAdapter<T>.() -> YasuoRVAdapter<T>
): YasuoRVAdapter<T> {
    return YasuoRVAdapter(context, life, list).bindLife().rvListener().attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun <T : Any> RecyclerView.rvAdapter(
    adapter: YasuoRVAdapter<T>,
    rvListener: YasuoRVAdapter<T>.() -> YasuoRVAdapter<T>
): YasuoRVAdapter<T> {
    return adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVAdapter<T : Any>(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<T>
) : YasuoBaseRVAdapter<T, RecyclerViewHolder>(context, itemList), LifecycleObserver {
    private val dataInvalidation = Any()

    constructor(context: Context, life: LifecycleOwner) : this(
        context, life,
        ObList()
    )

    init {
        //如果是使用的ObservableArrayList，那么需要注册监听
        if (this.itemList is ObList<T>) {
            (this.itemList as ObList<T>).addOnListChangedCallback(listener)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun itemsRemoveListener() {
        if (this.itemList is ObList<T>) {
            (this.itemList as ObList).removeOnListChangedCallback(listener)
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
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListener<RecyclerViewHolder>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewHolder>> setHolderCreateListener(
        type: Int,
        listener: L
    ) {
        innerHolderCreateListenerMap.put(
            type,
            listener as ViewHolderCreateListener<RecyclerViewHolder>
        )
    }

    override fun <L : Listener<RecyclerViewHolder>> setHolderBindListener(
        type: Int,
        listener: L
    ) {
        innerHolderBindListenerMap.put(
            type,
            listener as ViewHolderBindListener<RecyclerViewHolder>
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val holder = RecyclerViewHolder(inflater.inflate(viewType, parent, false))
        innerHolderCreateListenerMap[viewType]?.apply {
            onCreateViewHolder(holder)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (!disableGlobalItemHolderListenerType(holder.itemViewType)) {
            //执行之前判断非空
            getGlobalItemHolderListener()?.invoke(holder)
        }
        innerHolderBindListenerMap[holder.itemViewType]?.onBindViewHolder(holder)
    }

    override fun onBindViewHolder(
        holder: RecyclerViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (isValidPayLoads(payloads)) {
            onBindViewHolder(holder, position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun isValidPayLoads(payloads: List<Any>): Boolean {
        if (payloads.isEmpty()) {
            return false
        }
        payloads.forEach {
            if (it != dataInvalidation) {
                return false
            }
        }
        return true
    }
}

/**
 * View Holder创建时触发
 */
inline fun <T : Any, Adapter : YasuoRVAdapter<T>>
        Adapter.onHolderCreate(
    itemLayoutId: Int,
    crossinline block: (holder: RecyclerViewHolder) -> Unit
): Adapter {
    setHolderCreateListener(
        itemLayoutId,
        object :
            ViewHolderCreateListener<RecyclerViewHolder> {
            override fun onCreateViewHolder(
                holder: RecyclerViewHolder
            ) {
                block(holder)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>>
        Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bind: (holder: RecyclerViewHolder, payloads: List<Any>?) -> Unit
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListener<RecyclerViewHolder> {
            override fun onBindViewHolder(
                holder: RecyclerViewHolder,
                payloads: List<Any>?
            ) {
                bind(holder, payloads)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>>
        Adapter.onHolderBind(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bind: (holder: RecyclerViewHolder) -> Unit
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListener<RecyclerViewHolder> {
            override fun onBindViewHolder(
                holder: RecyclerViewHolder,
                payloads: List<Any>?
            ) {
                bind(holder)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，该方法用于仅绑定
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 */
fun <T : Any, Adapter : YasuoRVAdapter<T>>
        Adapter.onHolderBind(
    itemLayoutId: Int,
    kClass: KClass<*>
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    return this
}

/*  写法
testRV.rvAdapter(this, this, viewModel.data.list) {
            holderBind(R.layout.test_item_text, TestTextItem::class) { holder ->
                val item = items.getFormat<TestTextItem>(holder.bindingAdapterPosition)
                tv.text = item.text
            }
            holderBind(R.layout.test_item_text2, TestTextItem2::class) { holder ->
                val item = items.getFormat<TestTextItem2>(holder.bindingAdapterPosition)
                holder.addChangeFun(tv1.id, item.text1) {
                    item.text1 = it
                    tv1.text = item.text1
                }.addChangeFun(tv2.id, item.text2) {
                    item.text2 = it
                    tv2.text = item.text2
                }
                holder.itemView.setOnClickListener {
                    items.remove(item)
                }
            }
        }
*/

