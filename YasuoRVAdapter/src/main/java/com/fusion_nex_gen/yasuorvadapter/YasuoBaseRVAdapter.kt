package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.interfaces.Factory
import com.fusion_nex_gen.yasuorvadapter.interfaces.LayoutFactory
import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener
import kotlin.reflect.KClass


/**
 * RecyclerView适配器基类
 *
 * TODO 开发计划
 * 1、List，Grid，StaggeredGrid类型的正常布局及多布局的基础配置，dataBinding配置及DSL
 * 2、动画的高可配置(采用recyclerView的itemAnimator方案，详见mikepenz/ItemAnimators库)
 * 3、空白页/头部/尾部
 * 4、加载更多
 * TODO 5、折叠布局
 * 6、拖拽、横向滑动删除
 * TODO 7、横向滑动显示选项
 */
abstract class YasuoBaseRVAdapter<T : Any, VH : RecyclerView.ViewHolder>(context: Context) :
    RecyclerView.Adapter<VH>() {

    constructor(context: Context, newItems: MutableList<T>?) : this(context) {
        if (newItems != null) {
            this.itemList = newItems
        }
    }

    /**
     * 列表
     */
    var itemList: MutableList<T> = mutableListOf()

    /**
     * 列表所有类型的集合，实体类kClass作为key，类型type作为value
     */
    internal val itemTypes: MutableMap<KClass<*>, ItemType> = mutableMapOf()

    /**
     * RV
     */
    internal var recyclerView: RecyclerView? = null

    /**
     * 内部布局拦截器
     */
    internal var innerLayoutFactory: LayoutFactory? = null

    /**
     * 布局创建器
     */
    internal val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * 拖拽/侧滑删除
     */
    var itemTouchHelper: ItemTouchHelper? = null

    /******全屏布局******/

    /**
     * 全屏布局
     * Full screen layout
     */
    var fullScreenLayoutId: Int? = null

    /**
     * 全屏布局的实体数据
     * Entity data of full screen layout
     */
    var fullScreenLayoutItem: Any? = null

    /**
     * 判断当前是否是显示全屏布局状态
     * Judge whether the current display is full screen layout state
     */
    fun isFullScreenMode(): Boolean {
        // 当itemList为空，并且已经设置全屏布局的数据时
        // 则此时为显示全屏布局的状态
        // When itemList is empty and full screen layout data has been set
        // The full screen layout is displayed
        return itemList.isNullOrEmpty() && alreadySetFullScreenData()
    }

    /**
     * 是否已经设置好了全屏布局的数据
     */
    internal fun alreadySetFullScreenData(): Boolean {
        return fullScreenLayoutId != null && fullScreenLayoutItem != null
    }

    /**
     * 设置全屏布局，该方法会强制将itemList清空
     * Set the full screen layout, which forces the itemList to be cleared
     * @param type 全屏布局类型
     * @param type Full screen layout type
     */
    fun showFullScreen() {
        if (!alreadySetFullScreenData()) {
            throw RuntimeException("Data without full screen layout")
        }
        lockedLoadMore = true
        //清空itemList
        //clear itemList
        if (itemList.isNotEmpty()) {
            itemList.clear()
        }
        //添加全屏布局
        notifyItemInserted(0)
    }

    /******加载更多******/

    /**
     * 锁定加载更多，如果为true，那么不再触发监听
     */
    var lockedLoadMore = false

    /**
     * 加载更多的布局类型
     */
    var loadMoreLayoutId: Int? = null

    /**
     * 加载更多的布局类型数据实体
     */
    var loadMoreLayoutItem: Any? = null

    fun hasLoadMore(): Boolean {
        return loadMoreLayoutId != null && loadMoreLayoutItem != null
    }

    /**
     * 停止加载
     * 必须在添加数据之前调用
     */
    fun stopLoadMore() {
        notifyItemRemoved(itemList.size)
    }

    /******item相关******/

    /**
     * 获取item数量
     * Get the counts of items
     */
    override fun getItemCount(): Int {
        //如果是全屏布局模式
        if (isFullScreenMode()) {
            return 1
        }
        //如果有loadMore
        if (hasLoadMore()) {
            return itemList.size + 1
        }
        return itemList.size
    }

    /**
     * 判断position只包含在itemList内
     */
    internal fun inRange(position: Int): Boolean {
        return position >= 0 && position < itemList.size
    }


    /**
     * 根据position获取item
     * Get item according to position
     * @param position holder.bindingAdapterPosition
     */
    open fun getItem(position: Int): T {
        if (inRange(position)) {
            return itemList[position]
        } else {
            throw RuntimeException("The position is not in the itemList")
        }
    }

    /**
     * 防止itemView闪烁
     * Prevent ItemView from flashing
     * @param position holder.bindingAdapterPosition
     */
    override fun getItemId(position: Int): Long {
        return if (hasStableIds()) {
            (getItem(position).hashCode() + position).toLong()
        } else {
            super.getItemId(position)
        }
    }

    /**
     * 获取item的类型（类型即为布局id）
     * @param position holder.bindingAdapterPosition
     */
    override fun getItemViewType(position: Int): Int {
        //先判断全屏布局
        //如果是全屏模式，那么直接返回全屏viewType
        if (isFullScreenMode()) {
            return fullScreenLayoutId!!
        }
        //再判断loadMoreView
        if (position == itemList.size) {
            return loadMoreLayoutId!!
        }
        //否则就是itemList中的type
        val itemType = itemTypes[getItem(position)::class]
        if (itemType != null) {
            return itemType.itemLayoutId
        }
        if (innerLayoutFactory != null) {
            return innerLayoutFactory!!.run {
                getLayoutId(position)
            }
        }
        throw RuntimeException("viewType not found")
    }

    /******布局占比相关******/

    /**
     * 在GridLayoutManager模式下，指定item在当前列或行的占比
     * In GridLayoutManager mode, specify the proportion of items in the current column or row
     */
    var spanSizeLookup: GridLayoutManager.SpanSizeLookup? = null

    /**
     * 此方法针对一些需要强制性占满或无法在onAttachedToRecyclerView中实现占满的item
     * 可重写此方法来添加header、footer，或者自定义loadMoreView、全屏布局的viewType
     * @param viewType Layout type
     */
    open fun isFullSpanType(viewType: Int): Boolean {
        //默认判断loadMoreViewType和全屏布局的type
        return loadMoreLayoutId == viewType || fullScreenLayoutId == viewType
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        if (isFullSpanType(holder.itemViewType)) {
            val lp = holder.itemView.layoutParams
            if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
                lp.isFullSpan = true
            }
        }
    }


    //绑定到recyclerView
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (!inRange(position)) {
                        return manager.spanCount
                    }
                    val type = getItemViewType(position)
                    return when {
                        isFullSpanType(type) -> manager.spanCount
                        spanSizeLookup == null -> 1
                        else -> spanSizeLookup!!.getSpanSize(position)
                    }
                }
            }
        }
    }

    /**
     * 解绑
     */
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    /**
     * 添加holder创建监听
     * innerHolderCreateListenerMap.put(type, listener)
     */
    abstract fun <L : Listener<VH>> setHolderCreateListener(type: Int, listener: L)

    /**
     * 添加holder绑定监听
     * innerHolderBindListenerMap.put(type, listener)
     */
    abstract fun <L : Listener<VH>> setHolderBindListener(type: Int, listener: L)

    open fun setInnerFactory(factory: Factory) {
        when (factory) {
            is LayoutFactory -> innerLayoutFactory = factory
        }
    }


    /******列表改变的监听******/

    /**
     * 列表改变的监听
     * Monitoring of list changes
     */
    val listener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(contributorViewModels: ObservableList<T>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            notifyItemRangeChanged(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i, i1)
            }
            afterDataChangeListener?.invoke()
        }
    }

    /**
     * 列表数据发生改变后的监听，在notify之后触发
     * After the list data changes, the monitor will be triggered after notify
     */
    private var afterDataChangeListener: (() -> Unit)? = null

    fun setAfterDataChangeListener(listener: () -> Unit) {
        afterDataChangeListener = listener
    }


    /******全局item监听******/

    /**
     * 针对item的全局监听
     */
    private var globalItemHolderListener: ((holder: VH) -> Unit)? =
        null

    fun getGlobalItemHolderListener() = globalItemHolderListener

    fun setGlobalItemHolderListener(listener: (holder: VH) -> Unit) {
        globalItemHolderListener = listener
    }

    /**
     * 重写此方法，设置禁用全局监听的布局类型
     * Override this method to set the layout type to disable global listening
     * @param viewType 布局类型
     * @param viewType Layout type
     */
    open fun disableGlobalItemHolderListenerType(viewType: Int): Boolean {
        return false
    }

}

/**
 * 绑定适配器
 */
fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>, RV : RecyclerView> Adapter.attach(rv: RV): Adapter {
    rv.adapter = this
    return this
}


/**
 * 当为GridLayoutManager的时候，设置item的占用比例
 */
fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>> Adapter.setSpan(
    spanSizeLookup: (item: T, position: Int) -> Int
): Adapter {
    this.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return spanSizeLookup(itemList[position], position)
        }
    }
    return this
}

fun <T> List<Any>.getFormat(position: Int): T {
    return get(position) as T
}
