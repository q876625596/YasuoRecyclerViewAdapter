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
inline fun <T : Any> RecyclerView.rvViewBindingAdapter(
    context: Context,
    life: LifecycleOwner,
    list: MutableList<T>,
    rvListener: YasuoRVViewBindingAdapter<T>.() -> YasuoRVViewBindingAdapter<T>
): YasuoRVViewBindingAdapter<T> {
    return YasuoRVViewBindingAdapter<T>(context, life, list).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun <T : Any> RecyclerView.rvViewBindingAdapter(
    adapter: YasuoRVViewBindingAdapter<T>,
    rvListener: YasuoRVViewBindingAdapter<T>.() -> YasuoRVViewBindingAdapter<T>
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVViewBindingAdapter<T : Any>(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<T>
) : YasuoBaseRVAdapter<T, RecyclerViewBindingHolder<ViewBinding>>(context, itemList),
    LifecycleObserver {
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
    fun itemListRemoveListener() {
        if (this.itemList is ObList<T>) {
            (this.itemList as ObList<T>).removeOnListChangedCallback(listener)
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
    private val innerHolderCreateListenerMap: SparseArray<ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>>> =
        SparseArray()

    //内部holder绑定的监听集合
    private val innerHolderBindListenerMap: SparseArray<ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>>> =
        SparseArray()

    override fun <L : Listener<RecyclerViewBindingHolder<ViewBinding>>> setHolderCreateListener(
        type: Int,
        listener: L
    ) {
        innerHolderCreateListenerMap.put(
            type,
            listener as ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>>
        )
    }

    override fun <L : Listener<RecyclerViewBindingHolder<ViewBinding>>> setHolderBindListener(
        type: Int,
        listener: L
    ) {
        innerHolderBindListenerMap.put(
            type,
            listener as ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>>
        )
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewBindingHolder<ViewBinding> {
        val holder = RecyclerViewBindingHolder<ViewBinding>(
            inflater.inflate(
                viewType,
                parent,
                false
            )
        )
        //执行holder创建时的监听
        innerHolderCreateListenerMap[viewType]?.apply {
            onCreateViewHolder(holder)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: RecyclerViewBindingHolder<ViewBinding>,
        position: Int
    ) {
        //非禁用全局监听的布局才执行
        if (!disableGlobalItemHolderListenerType(holder.itemViewType)) {
            //执行之前判断非空
            getGlobalItemHolderListener()?.invoke(holder)
        }
        innerHolderBindListenerMap[holder.itemViewType]?.apply {
            onBindViewHolder(holder)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerViewBindingHolder<ViewBinding>,
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
inline fun <T : Any, Adapter : YasuoRVViewBindingAdapter<T>>
        Adapter.onHolderCreate(
    itemLayoutId: Int,
    crossinline block: (holder: RecyclerViewBindingHolder<ViewBinding>) -> Unit
): Adapter {
    setHolderCreateListener(
        itemLayoutId,
        object :
            ViewHolderCreateListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>> {
            override fun onCreateViewHolder(
                holder: RecyclerViewBindingHolder<ViewBinding>,
            ) {
                block(holder)
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
fun <T, VB : ViewBinding, Adapter : YasuoRVViewBindingAdapter<T>>
        Adapter.onHolderBindAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindListener: (holder: RecyclerViewBindingHolder<ViewBinding>, payloads: List<Any>?) -> Unit
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>> {
            override fun onBindViewHolder(
                holder: RecyclerViewBindingHolder<ViewBinding>,
                payloads: List<Any>?
            ) {
                bindListener(holder, payloads)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindListener 绑定监听这个viewHolder的所有事件
 */
fun <T, Adapter : YasuoRVViewBindingAdapter<T>>
        Adapter.onHolderBind(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindListener: (holder: RecyclerViewBindingHolder<ViewBinding>) -> Unit
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListenerForViewBinding<RecyclerViewBindingHolder<ViewBinding>> {
            override fun onBindViewHolder(
                holder: RecyclerViewBindingHolder<ViewBinding>,
                payloads: List<Any>?
            ) {
                bindListener(holder)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，该方法用于仅绑定
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 */
fun <T, Adapter : YasuoRVViewBindingAdapter<T>>
        Adapter.onHolderBind(
    itemLayoutId: Int,
    kClass: KClass<*>,
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId)
    return this
}
