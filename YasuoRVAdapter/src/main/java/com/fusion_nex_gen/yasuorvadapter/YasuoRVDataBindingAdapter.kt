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
inline fun <T : Any> RecyclerView.rvDataBindingAdapter(
    context: Context,
    life: LifecycleOwner,
    list: MutableList<T>,
    rvListener: YasuoRVDataBindingAdapter<T>.() -> YasuoRVDataBindingAdapter<T>
): YasuoRVDataBindingAdapter<T> {
    return YasuoRVDataBindingAdapter<T>(context, life, list).bindLife().rvListener()
        .attach(this)
}

/**
 * 绑定adapter
 * @param adapter Adapter实体
 * @param adapter Adapter实体 entity
 * @param rvListener 绑定Adapter实体之前需要做的操作
 * @param rvListener What to do before binding adapter entity
 */
inline fun <T : Any> RecyclerView.rvDataBindingAdapter(
    adapter: YasuoRVDataBindingAdapter<T>,
    rvListener: YasuoRVDataBindingAdapter<T>.() -> YasuoRVDataBindingAdapter<T>
) {
    adapter.bindLife().rvListener().attach(this)
}

open class YasuoRVDataBindingAdapter<T : Any>(
    context: Context,
    private val life: LifecycleOwner,
    itemList: MutableList<T>
) : YasuoBaseRVAdapter<T, RecyclerDataBindingHolder<ViewDataBinding>>(context, itemList),
    LifecycleObserver {
    private val dataInvalidation = Any()

    /**
     * 如果为true，那么布局中的variableId默认为BR.item，可以提升性能
     */
    var variableIdIsDefault = true

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
    fun bindLife(): YasuoRVDataBindingAdapter<T> {
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

    override fun <L : Listener<RecyclerDataBindingHolder<ViewDataBinding>>> setHolderCreateListener(
        type: Int,
        listener: L
    ) {
        innerHolderCreateListenerMap.put(
            type,
            listener as ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>
        )
    }

    override fun <L : Listener<RecyclerDataBindingHolder<ViewDataBinding>>> setHolderBindListener(
        type: Int,
        listener: L
    ) {
        innerHolderBindListenerMap.put(
            type,
            listener as ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>>
        )
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerDataBindingHolder<ViewDataBinding> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            inflater,
            viewType,
            parent,
            false
        )
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
        innerHolderCreateListenerMap[viewType]?.apply {
            onCreateViewHolder(holder, binding)
        }
        return holder
    }

    override fun onBindViewHolder(
        holder: RecyclerDataBindingHolder<ViewDataBinding>,
        position: Int
    ) {
        //非禁用全局监听的布局才执行
        if (!disableGlobalItemHolderListenerType(holder.itemViewType)) {
            //执行之前判断非空
            getGlobalItemHolderListener()?.invoke(holder)
        }
        when {
            //判断是全屏布局
            isFullScreenMode() -> holder.binding.setVariable(BR.item, fullScreenLayoutItem)
            //判断loadMoreView
            position == itemList.size -> holder.binding.setVariable(BR.item, loadMoreLayoutItem)
            //普通item
            else -> {
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
        }
        innerHolderBindListenerMap[holder.itemViewType]?.apply {
            onBindViewHolder(holder, holder.binding)
        }
        holder.binding.lifecycleOwner = life
        holder.binding.executePendingBindings()
    }

    override fun onBindViewHolder(
        holder: RecyclerDataBindingHolder<ViewDataBinding>,
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
inline fun <T, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter<T>>
        Adapter.onHolderCreate(
    itemLayoutId: Int,
    bindingType: KClass<VB>,
    crossinline block: VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit
): Adapter {
    setHolderCreateListener(
        itemLayoutId,
        object :
            ViewHolderCreateListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onCreateViewHolder(
                holder: RecyclerDataBindingHolder<ViewDataBinding>,
                binding: ViewDataBinding
            ) {
                (binding as VB).block(holder)
            }
        })
    return this
}

/*dataBinding*/

/**
 * 建立数据类与布局文件之间的匹配关系，payloads
 * @param itemLayoutId itemView布局id
 * @param kClass Item::class
 * @param bindingType ViewDataBinding::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter<T>>
        Adapter.onHolderDataBindingAndPayloads(
    itemLayoutId: Int,
    kClass: KClass<*>,
    bindingType: KClass<VB>,
    customItemBR: Int = BR.item,
    bind: VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>, payloads: List<Any>?) -> Unit
): Adapter {
    itemTypes[kClass] = ItemType(itemLayoutId, customItemBR)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onBindViewHolder(
                holder: RecyclerDataBindingHolder<ViewDataBinding>,
                binding: ViewDataBinding,
                payloads: List<Any>?
            ) {
                (binding as VB).bind(holder, payloads)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系
 * @param itemLayoutId itemView布局id
 * @param itemClass Item::class
 * @param bindingClass ViewDataBinding::class
 * @param bind 绑定监听这个viewHolder的所有事件
 */
fun <T, VB : ViewDataBinding, Adapter : YasuoRVDataBindingAdapter<T>>
        Adapter.onHolderDataBinding(
    itemLayoutId: Int,
    itemClass: KClass<*>,
    bindingClass: KClass<VB>,
    customItemBR: Int = BR.item,
    bind: (VB.(holder: RecyclerDataBindingHolder<ViewDataBinding>) -> Unit) = {}
): Adapter {
    itemTypes[itemClass] = ItemType(itemLayoutId, customItemBR)
    setHolderBindListener(
        itemLayoutId,
        object : ViewHolderBindListenerForDataBinding<RecyclerDataBindingHolder<ViewDataBinding>> {
            override fun onBindViewHolder(
                holder: RecyclerDataBindingHolder<ViewDataBinding>,
                binding: ViewDataBinding,
                payloads: List<Any>?
            ) {
                (binding as VB).bind(holder)
            }
        })
    return this
}

/**
 * 建立数据类与布局文件之间的匹配关系，该方法用于仅绑定
 * @param itemLayoutId itemView布局id
 * @param itemClass Item::class
 */
fun <T, Adapter : YasuoRVDataBindingAdapter<T>>
        Adapter.onHolderDataBinding(
    itemLayoutId: Int,
    itemClass: KClass<*>,
    customItemBR: Int = BR.item,
): Adapter {
    itemTypes[itemClass] = ItemType(itemLayoutId, customItemBR)
    return this
}


