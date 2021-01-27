package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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

    constructor(
        context: Context,
        itemList: MutableList<T>,
        headerItemList: MutableList<T>,
        footerItemList: MutableList<T>,
    ) : this(context) {
        this.itemList = itemList
        this.headerItemList = headerItemList
        this.footerItemList = footerItemList
    }

    internal val dataInvalidation = Any()

    /**
     * item列表
     */
    var itemList: MutableList<T> = mutableListOf()

    /**
     * header列表
     */
    var headerItemList: MutableList<T> = mutableListOf()

    /**
     * footer列表
     */
    var footerItemList: MutableList<T> = mutableListOf()


    /**
     * 所有列表类型的集合，实体类[KClass]作为key，类型[ItemType]作为value
     */
    internal val itemTypes: MutableMap<KClass<*>, ItemType> = mutableMapOf()

    /**
     * RV
     */
    internal var recyclerView: RecyclerView? = null

    /**
     * 布局创建器
     */
    internal val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * 拖拽/侧滑删除
     */
    internal var itemTouchHelper: ItemTouchHelper? = null

    /******空布局******/

    /**
     * 空布局资源id，等同于viewType
     */
    internal var emptyLayoutId: Int? = null

    /**
     * 空布局的实体数据
     */
    internal var emptyLayoutItem: T? = null

    fun setEmptyLayout(emptyLayoutId: Int, emptyLayoutItem: T) {
        this.emptyLayoutId = emptyLayoutId
        this.emptyLayoutItem = emptyLayoutItem
    }

    /**
     * 判断当前是否是显示空布局状态
     */
    fun isEmptyLayoutMode(): Boolean {
        // 当itemList为空，并且已经设置全屏布局的数据时
        // 则此时为显示全屏布局的状态
        // When itemList is empty and full screen layout data has been set
        // The full screen layout is displayed
        return itemList.isNullOrEmpty() && alreadySetEmptyLayoutData()
    }

    /**
     * 是否已经设置好了空布局的数据
     */
    internal fun alreadySetEmptyLayoutData(): Boolean {
        return emptyLayoutId != null && emptyLayoutItem != null
    }

    /**
     * 设置空布局，该方法会强制将itemList清空，并锁定loadMore的监听
     */
    fun showEmptyLayout() {
        if (!alreadySetEmptyLayoutData()) {
            throw RuntimeException("Data without full screen layout")
        }
        lockedLoadMoreListener = true
        //清空itemList
        //clear itemList
        if (itemList.isNotEmpty()) {
            itemList.clear()
        }
        //添加空布局
        notifyItemInserted(0)
    }

    /******加载更多******/

    /**
     * 锁定加载更多，如果为true，那么不再触发监听
     */
    var lockedLoadMoreListener = false

    /**
     * 加载更多的布局类型
     */
    internal var loadMoreLayoutId: Int? = null

    /**
     * 加载更多的布局类型数据实体
     */
    internal var loadMoreLayoutItem: T? = null

    fun setLoadMoreLayout(loadMoreLayoutId: Int, loadMoreLayoutItem: T) {
        this.loadMoreLayoutId = loadMoreLayoutId
        this.loadMoreLayoutItem = loadMoreLayoutItem
    }

    fun hasLoadMore(): Boolean {
        return loadMoreLayoutId != null && loadMoreLayoutItem != null
    }

    /******item相关******/

    /**
     * 获取item数量
     * Get the counts of items
     */
    override fun getItemCount(): Int {
        //如果是全屏布局模式
        if (isEmptyLayoutMode()) {
            return 1
        }
        //如果有loadMore
        if (hasLoadMore()) {
            return getAllListSize() + 1
        }
        return getAllListSize()
    }

    /**
     * 获取全部列表的长度
     */
    fun getAllListSize() = headerItemList.size + itemList.size + footerItemList.size

    /**
     * 判断position包含在allList内
     */
    internal fun inAllList(position: Int): Boolean {
        return position >= 0 && position < getAllListSize()
    }

    /**
     * 判断position只包含在headerList内
     */
    internal fun inHeaderList(position: Int): Boolean {
        return position >= 0 && position < headerItemList.size
    }

    /**
     * 判断position只包含在itemList内
     */
    internal fun inItemList(position: Int): Boolean {
        return position >= headerItemList.size && position < itemList.size + headerItemList.size
    }

    /**
     * 判断position只包含在footerList内
     */
    internal fun inFooterList(position: Int): Boolean {
        return position >= headerItemList.size + itemList.size && position < getAllListSize()
    }


    /**
     * 根据position获取item
     * Get item according to position
     * @param position holder.bindingAdapterPosition
     */
    open fun getItem(position: Int): T {
        return when {
            isEmptyLayoutMode() -> emptyLayoutItem!!
            inHeaderList(position) -> headerItemList[position]
            inItemList(position) -> itemList[position - headerItemList.size]
            inFooterList(position) -> footerItemList[position - itemList.size - headerItemList.size]
            hasLoadMore() -> loadMoreLayoutItem!!
            else -> throw RuntimeException("The position is not in the itemList")
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
        return when {
            isEmptyLayoutMode() -> emptyLayoutId!!
            inAllList(position) -> {
                itemTypes[getItem(position)::class]?.itemLayoutId
                    ?: throw RuntimeException("viewType not found")
            }
            hasLoadMore() -> loadMoreLayoutId!!
            else -> throw RuntimeException("viewType not found")
        }
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
        return loadMoreLayoutId == viewType || emptyLayoutId == viewType
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
                    if (!inItemList(position)) {
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


    /******列表改变的监听******/

    /**
     * header列表改变的监听
     * Monitoring of header list changes
     */
    val headerListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
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
     * 列表改变的监听
     * Monitoring of list changes
     */
    val itemListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(contributorViewModels: ObservableList<T>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            notifyItemRangeChanged(i + headerItemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerItemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerItemList.size, i1 + headerItemList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerItemList.size, i1)
            }
            afterDataChangeListener?.invoke()
        }
    }


    /**
     * footer列表改变的监听
     * Monitoring of footer list changes
     */
    val footerListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(contributorViewModels: ObservableList<T>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            notifyItemRangeChanged(i + headerItemList.size + itemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerItemList.size + itemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerItemList.size + itemList.size, i1 + headerItemList.size + itemList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerItemList.size + itemList.size, i1)
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

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
        if (isValidPayLoads(payloads)) {
            onBindViewHolder(holder, position)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    internal fun isValidPayLoads(payloads: List<Any>): Boolean {
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