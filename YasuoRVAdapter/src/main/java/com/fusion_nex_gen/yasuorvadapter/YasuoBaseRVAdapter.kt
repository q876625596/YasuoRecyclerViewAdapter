package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.bean.*
import com.fusion_nex_gen.yasuorvadapter.listener.YasuoVHListener
import com.fusion_nex_gen.yasuorvadapter.sticky.StickyCallBack
import kotlin.reflect.KClass


/**
 * RecyclerView适配器基类
 *
 * TODO 开发计划
 * 1、List，Grid，StaggeredGrid类型的正常布局及多布局的基础配置，dataBinding配置及DSL
 * 2、动画的高可配置(采用recyclerView的itemAnimator方案，详见mikepenz/ItemAnimators库)
 * 3、空白页/头部/尾部
 * 4、加载更多
 * 5、折叠布局
 * 6、拖拽、横向滑动删除
 * 7、吸顶(采用qiujayen/sticky-layoutmanager的方案，低耦合adapter和item，最低限度的修改代码)
 * TODO 8、横向滑动显示选项
 */
abstract class YasuoBaseRVAdapter<T : Any, VH : RecyclerView.ViewHolder>(context: Context) :
    RecyclerView.Adapter<VH>(), StickyCallBack {

    /**
     * @param context 上下文
     * @param itemList 实际列表
     * @param headerItemList 头部item列表
     * @param footerItemList 底部item列表
     */
    constructor(
        context: Context,
        itemList: YasuoList<T>,
        headerItemList: YasuoList<T>,
        footerItemList: YasuoList<T>,
        isFold: Boolean = false,
    ) : this(context) {
        this.itemList = itemList
        this.headerList = headerItemList
        this.footerList = footerItemList
        this.isFold = isFold
    }

    internal val dataInvalidation = Any()


    var isFold: Boolean = false

    /**
     * item列表
     */
    var itemList: YasuoList<T> = YasuoList()

    /**
     * header列表
     */
    var headerList: YasuoList<T> = YasuoList()

    /**
     * footer列表
     */
    var footerList: YasuoList<T> = YasuoList()


    /**
     * 所有列表类型的集合，实体类[KClass]作为key，类型[YasuoItemType]作为value
     */
    val itemTypes: MutableMap<KClass<*>, YasuoItemType> = mutableMapOf()

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
    private var emptyLayoutId: Int? = null
    fun getEmptyLayoutId() = emptyLayoutId

    /**
     * 空布局的实体数据
     */
    private var emptyLayoutItem: T? = null
    fun getEmptyLayoutItem() = emptyLayoutItem

    /**
     * 设置空布局
     * @param emptyLayoutId 空布局资源id，等同于viewType
     * @param emptyLayoutItem 空布局的实体数据
     */
    fun setEmptyLayout(emptyLayoutId: Int, emptyLayoutItem: T = Any() as T) {
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
    fun alreadySetEmptyLayoutData(): Boolean {
        return emptyLayoutId != null && emptyLayoutItem != null
    }

    /**
     * 如果[emptyLayoutItem]中没有使用[androidx.lifecycle.LiveData]来监听内容的改变
     * 那么需要调用此方法手动刷新emptyLayout显示的内容
     */
    fun refreshEmptyLayout(options: T.() -> Unit) {
        emptyLayoutItem?.apply {
            options()
        } ?: throw RuntimeException("emptyLayoutItem is null")
        notifyItemChanged(0,dataInvalidation)
    }

    /**
     * 设置空布局，该方法会强制将itemList清空，并锁定loadMore的监听
     */
    fun showEmptyLayout() {
        if (!alreadySetEmptyLayoutData()) {
            throw RuntimeException(
                "空布局数据未设置，\n" +
                        "Empty layout data is not set"
            )
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
     * 锁定加载更多的监听，如果为true，那么不再触发监听
     */
    var lockedLoadMoreListener = false

    /**
     * 加载更多的布局类型
     */
    internal var loadMoreLayoutId: Int? = null
    fun getLoadMoreLayoutId() = loadMoreLayoutId

    /**
     * 加载更多的布局类型数据实体
     */
    internal var loadMoreLayoutItem: T? = null
    fun getLoadMoreLayoutItem() = loadMoreLayoutItem

    /**
     * 设置加载更多布局与实体数据
     * @param loadMoreLayoutId 加载更多的布局类型
     * @param loadMoreLayoutItem 加载更多的布局类型数据实体
     */
    fun setLoadMoreLayout(loadMoreLayoutId: Int, loadMoreLayoutItem: T) {
        this.loadMoreLayoutId = loadMoreLayoutId
        this.loadMoreLayoutItem = loadMoreLayoutItem
    }


    /**
     * 如果[loadMoreLayoutItem]中没有使用[androidx.lifecycle.LiveData]来监听内容的改变
     * 那么需要调用此方法手动刷新loadMore显示的内容
     */
    fun refreshLoadMoreLayout(options: T.() -> Unit) {
        loadMoreLayoutItem?.apply {
            options()
        } ?: throw RuntimeException("loadMoreLayoutItem is null")
        notifyItemChanged(getAllListSize())
    }

    /**
     * 设置了loadMore的布局与数据
     */
    fun hasLoadMore(): Boolean {
        return loadMoreLayoutId != null && loadMoreLayoutItem != null
    }

    /******item相关******/

    /**
     * 获取可显示的所有的item数量
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
    fun getAllListSize() = headerList.size + itemList.size + footerList.size

    /**
     * 获取headerList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getHeaderTruePosition(position: Int) = position

    /**
     * 获取itemList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getItemTruePosition(position: Int) = position - headerList.size

    /**
     * 获取footerList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getFooterTruePosition(position: Int) = position - headerList.size - itemList.size

    /**
     * 判断position是否包含在headerList，itemList，footerList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inAllList(position: Int): Boolean {
        return position in 0 until getAllListSize()
    }

    /**
     * 判断position只包含在headerList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inHeaderList(position: Int): Boolean {
        return position in 0 until headerList.size
    }

    /**
     * 判断position只包含在itemList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inItemList(position: Int): Boolean {
        return position in headerList.size until itemList.size + headerList.size
    }

    /**
     * 判断position只包含在footerList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inFooterList(position: Int): Boolean {
        return position in headerList.size + itemList.size until getAllListSize()
    }


    /**
     * 根据position获取item
     * Get item according to position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    open fun getItem(position: Int): T {
        return when {
            isEmptyLayoutMode() -> emptyLayoutItem!!
            inHeaderList(position) -> headerList[getHeaderTruePosition(position)]
            inItemList(position) -> itemList[getItemTruePosition(position)]
            inFooterList(position) -> footerList[getFooterTruePosition(position)]
            hasLoadMore() -> loadMoreLayoutItem!!
            else -> throw RuntimeException(
                "该position找不到对应实体，position = ${position}，\n" +
                        "The position cannot find a corresponding entity,position = $position"
            )
        }
    }

    /**
     * 防止itemView闪烁
     * Prevent ItemView from flashing
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
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
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    override fun getItemViewType(position: Int): Int {
        return when {
            isEmptyLayoutMode() -> emptyLayoutId!!
            inAllList(position) -> itemTypes[getItem(position)::class]?.itemLayoutId
                ?: throw RuntimeException(
                    "找不到viewType，position = ${position}，\n" +
                            "viewType not found,position = $position"
                )
            hasLoadMore() -> loadMoreLayoutId!!
            else -> throw RuntimeException(
                "找不到viewType，position = ${position}，\n" +
                        "viewType not found,position = $position"
            )
        }
    }


    /******折叠布局******/

    /**
     * 展开/折叠某个item
     * @param item 需要展开的item
     */
    fun expandOrFoldItem(item: Any, forceNotify: Boolean = false) {
        //首先判断该item是否继承于FoldItem
        if (item !is YasuoFoldItem) {
            throw RuntimeException(
                "实体类必须继承FoldItem，\n" +
                        "The entity class must extend FoldItem"
            )
        }
        //如果下一级列表为 空，那么不做任何操作
        if (item.list.isEmpty()) {
            return
        }
        val position = itemList.indexOf(item as T)
        //如果已经展开，那么收起
        if (item.isExpand) {
            item.isExpand = false
            itemList.removeFrom(position + 1, position + 1 + item.list.size)
            if (forceNotify) {
                notifyItemRangeRemoved(position + 1, item.list.size)
            }
            return
        }
        //如果未展开，那么展开
        item.isExpand = true
        //展开的同时给子级的parentHash赋值
        item.list.forEach {
            if (it.parentHash == null) {
                it.parentHash = item.hashCode()
            }
        }
        itemList.addAll(position + 1, item.list as YasuoList<T>)
        if (forceNotify) {
            notifyItemRangeInserted(position + 1, item.list.size)
        }

    }

    /**
     * 如果有操作需要删除展开列表中的子级item
     * 则需要在list.remove之前调用此方法
     * @param item 需要删除的item
     */
    fun removeFoldListItem(item: Any) {
        //首先判断该item是否继承于FoldItem
        if (item !is YasuoFoldItem) {
            throw RuntimeException(
                "实体类必须继承FoldItem，\n" +
                        "The entity class must extend FoldItem"
            )
        }
        //如果该子级item的parentHash为空，那么表示该item没有被展开显示过，那么抛出异常提示不能使用该方法删除
        if (item.parentHash == null) {
            throw RuntimeException(
                "不要使用此方法删除列表中未显示的子item，\n" +
                        "Do not use this method to delete child items that are not displayed in the list"
            )
        }
        //如果找到该子项，那么删除
        (itemList.find {
            it.hashCode() == item.parentHash
        } as? YasuoFoldItem)?.list?.remove(item)
    }

    /******布局占比相关******/

    /**
     * 在GridLayoutManager模式下，指定item在当前列或行的占比
     * In GridLayoutManager mode, specify the proportion of items in the current column or row
     */
    var itemGridSpan: ((position: Int) -> Int)? = null

    /**
     * 在GridLayoutManager模式下，指定header在当前列或行的占比
     * In GridLayoutManager mode, specify the proportion of headers in the current column or row
     */
    var headerGridSpan: ((position: Int) -> Int)? = null

    /**
     * 在GridLayoutManager模式下，指定footer在当前列或行的占比
     * In GridLayoutManager mode, specify the proportion of footers in the current column or row
     */
    var footerGridSpan: ((position: Int) -> Int)? = null

    /**
     * 此方法针对StaggeredGridLayoutManager实现占满的item
     * 可重写此方法来添加header、footer，或者自定义loadMoreView、全屏布局的viewType
     */
    var staggeredGridFullSpan: (position: Int, viewType: Int) -> Boolean = { position, viewType ->
        loadMoreLayoutId == viewType || emptyLayoutId == viewType
                || inHeaderList(position) || inFooterList(position)
    }

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        //这里用于判断StaggeredGridLayoutManager的item是否占满一行
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
            if (staggeredGridFullSpan(holder.bindingAdapterPosition, holder.itemViewType)) {
                lp.isFullSpan = true
            } else {
                //如果是sticky
                if (isSticky != null) {
                    if (isSticky!!(holder.bindingAdapterPosition)) {
                        lp.isFullSpan = true
                    }
                }
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
                    //优先级：特殊布局 > isSticky > item.sticky > gridSpan > item.span
                    //如果是空布局，那么也默认占满一行
                    if (isEmptyLayoutMode()) {
                        return manager.spanCount
                    }
                    //判断是loadMore
                    if (position == getAllListSize()) {
                        return manager.spanCount
                    }
                    //如果设置了sticky的判断方法
                    //并且该position通过判断
                    if (isSticky != null && isSticky!!(position)) {
                        return manager.spanCount
                    }
                    val item = getItem(position)
                    //如果是头部
                    if (inHeaderList(position)) {
                        //先判断是否继承基类
                        return if (item is YasuoHeaderItem) {
                            //item.sticky -> headerGridSpan -> item.span
                            if (item.sticky) manager.spanCount else headerGridSpan?.invoke(position) ?: item.span
                        } else {
                            //否则以headerGridSpan为准，无任何设置时头部默认占满
                            headerGridSpan?.invoke(position) ?: manager.spanCount
                        }
                    }
                    //如果是尾部
                    if (inFooterList(position)) {
                        //先判断是否继承基类
                        return if (item is YasuoFooterItem) {
                            //item.sticky -> footerGridSpan -> item.span
                            if (item.sticky) manager.spanCount else footerGridSpan?.invoke(position) ?: item.span
                        } else {
                            //否则以footerGridSpan为准，无任何设置时尾部默认占满
                            footerGridSpan?.invoke(position) ?: manager.spanCount
                        }
                    }
                    //如果是中间列表
                    if (inItemList(position)) {
                        return when (item) {
                            //如果中间列表继承了YasuoNormalItem
                            is YasuoNormalItem -> if (item.sticky) manager.spanCount else itemGridSpan?.invoke(position) ?: item.span
                            //如果中间列表继承了YasuoFoldItem
                            is YasuoFoldItem -> if (item.sticky) manager.spanCount else itemGridSpan?.invoke(position) ?: item.span
                            //如果没有继承任何类
                            //则以itemGridSpan为准，无任何设置时中间列表默认一格
                            else -> itemGridSpan?.invoke(position) ?: 1
                        }
                    }
                    //如果以上都不是，默认占满1格
                    return 1
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
    abstract fun <L : YasuoVHListener<VH>> setHolderCreateListener(type: Int, listener: L)

    /**
     * 添加holder绑定监听
     * innerHolderBindListenerMap.put(type, listener)
     */
    abstract fun <L : YasuoVHListener<VH>> setHolderBindListener(type: Int, listener: L)

    var isSticky: ((position: Int) -> Boolean)? = null
    override fun isStickyHeader(position: Int): Boolean {
        return isSticky?.invoke(position) ?: false
    }
/*
    internal var stickyClickListener: StickyClickListener<VH>? = null
    internal var stickyHolderCreateListener: StickyViewHolderCreateListener<VH>? = null
    internal var stickyHolderBindListener: StickyViewHolderBindListener<VH>? = null
    abstract fun getStickyId(position: Int): Long


    abstract fun onCreateStickyViewHolder(parent: RecyclerView, position: Int): VH?


   abstract fun onBindStickyViewHolder(holder: VH, position: Int)*/

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
            notifyItemRangeChanged(i + headerList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerList.size, i1 + headerList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerList.size, i1)
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
            notifyItemRangeChanged(i + headerList.size + itemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerList.size + itemList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerList.size + itemList.size, i1 + headerList.size + itemList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerList.size + itemList.size, i1)
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

    override fun onBindViewHolder(holder: VH, position: Int, payloads: List<Any>) {
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
 * 绑定适配器
 */
fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>, RV : RecyclerView> Adapter.attach(rv: RV): Adapter {
    rv.adapter = this
    return this
}


/**
 * 当为GridLayoutManager的时候，设置item的占用比例
 */
fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>> Adapter.setGridSpan(
    spanEx: (position: Int) -> Int
): Adapter {
    this.itemGridSpan = spanEx
    return this
}

/**
 * 当为StaggeredGridLayoutManager的时候，设置item的是否占满
 */
fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>> Adapter.setStaggeredGridFullSpan(
    staggeredGridFullSpan: (position: Int, viewType: Int) -> Boolean
): Adapter {
    this.staggeredGridFullSpan = staggeredGridFullSpan
    return this
}

fun <T, VH, Adapter : YasuoBaseRVAdapter<T, VH>> Adapter.setSticky(
    isSticky: (position: Int) -> Boolean
): Adapter {
    this.isSticky = isSticky
    return this
}