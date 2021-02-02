package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoBaseItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoBaseItemConfig
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoList
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
 * TODO 9、新增一些额外的常用功能，比如结合下拉刷新之后显示一个一临时头部，提示刷新了多少条
 */
abstract class YasuoBaseRVAdapter<T : Any, VH : RecyclerView.ViewHolder, Config : YasuoBaseItemConfig<T, VH>>(context: Context) :
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
        loadMoreItem: T? = null,
    ) : this(context) {
        this.itemList = itemList
        this.headerList = headerItemList
        this.footerList = footerItemList
        this.loadMoreLayoutItem = loadMoreItem
    }

    internal val dataInvalidation = Any()

    /**
     * 是否全局都是折叠布局
     * 如果不是局部折叠，可在所有bind方法前直接给此属性赋值为true
     */
    internal var isAllFold: Boolean = false
    fun setAllFold() {
        isAllFold = true
    }

    /**
     * item列表
     */
    var itemList: YasuoList<T> = YasuoList()

    /**
     * emptyLayout列表
     */
    var emptyList: YasuoList<T> = YasuoList()

    /**
     * header列表
     */
    var headerList: YasuoList<T> = YasuoList()

    /**
     * footer列表
     */
    var footerList: YasuoList<T> = YasuoList()


    /**
     * 所有列表类型的集合，实体类[KClass]作为key，类型[YasuoBaseItemConfig]作为value
     * 通常是这样获取：itemClassTypes[getItem(position)::class]
     */
    val itemClassTypes: MutableMap<KClass<*>, Config> = mutableMapOf()

    /**
     * 所有列表类型的集合，layoutId作为key，类型[YasuoBaseItemConfig]作为value
     * 相当于[itemClassTypes]的复制品，为的是能通过layoutId来获取[YasuoBaseItemConfig]
     * 并不会增加太多内存，但这样会减少一些常用操作的耗时
     * 通常是这样获取：itemIdTypes[getItemViewType]
     */
    val itemIdTypes: MutableMap<Int, Config> = mutableMapOf()

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
     * 空布局的实体数据
     */
    private var emptyLayoutItem: T? = null
    fun getEmptyLayoutItem() = emptyLayoutItem

    /**
     * 判断当前是否是显示空布局状态
     */
    fun isShowEmptyLayout(): Boolean {
        // 当itemList为空，并且已经设置全屏布局的数据时
        // 则此时为显示全屏布局的状态
        // When itemList is empty and full screen layout data has been set
        // The full screen layout is displayed
        return itemList.isNullOrEmpty() && emptyList.isNotEmpty()
    }

    /**
     * 设置空布局，该方法会强制将itemList清空，并锁定loadMore的监听
     */
    fun showEmptyLayout(emptyItem: T? = null, clearHeader: Boolean = false, clearFooter: Boolean = false) {
        //先锁定loadMoreListener
        lockedLoadMoreListener = true
        if (emptyItem == null && emptyLayoutItem == null) {
            throw RuntimeException(
                "空布局数据未设置，\n" +
                        "Empty layout data is not set"
            )
        }
        emptyLayoutItem = emptyItem
            ?: emptyLayoutItem
                    ?: throw RuntimeException(
                "空布局数据未设置，\n" +
                        "Empty layout data is not set"
            )
        //这一步设置很重要
        //如果布局是StaggeredGridLayoutManager，并且当此时列表触底的时候
        //在列表顶部会出现空白，因此先让列表回滚1像素
        if (recyclerView?.layoutManager is StaggeredGridLayoutManager) {
            recyclerView?.scrollBy(0, -1)
        }
        //清空itemList
        //clear itemList
        if (itemList.isNotEmpty()) {
            itemList.clear()
        }
        if (clearHeader) {
            headerList.clear()
        }
        if (clearFooter) {
            footerList.clear()
        }
        emptyList.clear()
        emptyList.add(emptyLayoutItem)
    }

    /******加载更多******/

    /**
     * 锁定加载更多的监听，如果为true，那么不再触发监听
     */
    var lockedLoadMoreListener = false

    /**
     * 加载更多的布局类型数据实体
     */
    internal var loadMoreLayoutItem: T? = null
    fun getLoadMoreLayoutItem() = loadMoreLayoutItem

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
        return loadMoreLayoutItem != null
    }

    /******item相关******/

    /**
     * 获取可显示的所有的item数量
     */
    override fun getItemCount(): Int {
        //如果有loadMore
        if (hasLoadMore()) {
            //这里如果是显示空布局，那么就需要把loadMore隐藏
            return getAllListSize() + 1 - if (isShowEmptyLayout()) 1 else 0
        }
        return getAllListSize()
    }

    /**
     * 获取全部列表的长度
     */
    fun getAllListSize() = headerList.size + itemList.size + emptyList.size + footerList.size

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
     * 获取emptyList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getEmptyTruePosition(position: Int) = position - headerList.size - itemList.size

    /**
     * 获取footerList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getFooterTruePosition(position: Int) = position - emptyList.size - headerList.size - itemList.size

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
     * 判断position只包含在emptyList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inEmptyList(position: Int): Boolean {
        return position in headerList.size + itemList.size until headerList.size + itemList.size + emptyList.size
    }

    /**
     * 判断position只包含在footerList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inFooterList(position: Int): Boolean {
        return position in headerList.size + itemList.size + emptyList.size until getAllListSize()
    }


    /**
     * 根据position获取item
     * Get item according to position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    open fun getItem(position: Int): T {
        return when {
            inHeaderList(position) -> headerList[getHeaderTruePosition(position)]
            inItemList(position) -> itemList[getItemTruePosition(position)]
            inEmptyList(position) -> emptyList[getEmptyTruePosition(position)]
            inFooterList(position) -> footerList[getFooterTruePosition(position)]
            hasLoadMore() && position == getAllListSize() -> loadMoreLayoutItem!!
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
        return itemClassTypes[getItem(position)::class]?.itemLayoutId
            ?: throw RuntimeException(
                "找不到viewType，position = ${position}，\n" +
                        "viewType not found,position = $position"
            )
    }


    /******折叠布局******/

    /**
     * 展开/折叠某个item
     * @param item 需要展开的item
     */
    fun expandOrFoldItem(item: YasuoFoldItem) {
        //如果下一级列表为 空，那么不做任何操作
        if (item.list.isEmpty()) {
            return
        }
        //先获取该item在itemList中的位置
        val position = itemList.indexOf(item as T)
        //如果已经展开，那么收起
        if (item.isExpand) {
            item.isExpand = false
            //从该item位置+1的地方开始，长度为该item子级列表的数量
            itemList.removeFrom(position + 1, position + 1 + item.list.size)
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
    }

    /**
     * 如果有操作需要删除展开列表中的子级item
     * 则需要在list.remove之前调用此方法
     * @param childItem 需要删除的子级item
     */
    fun removeFoldChildListItem(childItem: Any) {
        //首先判断该item是否继承于FoldItem
        if (childItem !is YasuoFoldItem) {
            throw RuntimeException(
                "实体类必须继承FoldItem，\n" +
                        "The entity class must extend FoldItem"
            )
        }
        //如果该子级item的parentHash为空，那么表示该item没有被展开显示过，那么抛出异常提示不能使用该方法删除
        if (childItem.parentHash == null) {
            throw RuntimeException(
                "不要使用此方法删除列表中未显示的子item，\n" +
                        "Do not use this method to delete child items that are not displayed in the list"
            )
        }
        //如果找到该子项，那么删除
        (itemList.find {
            it.hashCode() == childItem.parentHash
        } as? YasuoFoldItem)?.list?.remove(childItem)
    }

    /******布局占比相关******/

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        //这里用于判断StaggeredGridLayoutManager的item是否占满一行
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
            //判断粒度由小到大，优先级sticky > staggeredGridFullSpan
            val position = holder.bindingAdapterPosition
            val item = getItem(position)
            //优先判断每一个item实例是否单独设置staggeredGridFullSpan或sticky
            if (item is YasuoBaseItem) {
                if (item.sticky) {
                    lp.isFullSpan = true
                    return
                }
                if (item.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
            }
            //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置staggeredGridFullSpan或sticky
            val itemConfig = itemIdTypes[holder.itemViewType]
                ?: throw RuntimeException("找不到Config")
            if (itemConfig.sticky) {
                lp.isFullSpan = true
                return
            }
            if (itemConfig.staggeredGridFullSpan) {
                lp.isFullSpan = true
                return
            }
            //最后头部，尾部，空布局，加载更多布局默认占满
            lp.isFullSpan = (loadMoreLayoutItem != null && itemClassTypes[loadMoreLayoutItem!!::class]?.itemLayoutId == holder.itemViewType)
                    || inEmptyList(position) || inHeaderList(position) || inFooterList(position)
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
                    //粒度由小到大，优先级sticky > gridSpan
                    //优先判断每一个item实例是否单独设置gridSpan或sticky
                    val item = getItem(position)
                    if (item is YasuoBaseItem) {
                        if (item.sticky) {
                            return manager.spanCount
                        }
                        if (item.gridSpan != 0) {
                            return item.gridSpan
                        }
                    }
                    //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置gridSpan或sticky
                    val itemConfig = itemClassTypes[item::class]
                        ?: throw RuntimeException("找不到对应Config")
                    //如果设置了sticky，那么占满
                    if (itemConfig.sticky) {
                        return manager.spanCount
                    }
                    if (itemConfig.gridSpan != 0) {
                        return itemConfig.gridSpan
                    }
                    //判断是loadMore
                    if (position == getAllListSize()) {
                        return manager.spanCount
                    }
                    //如果是头部尾部,空布局
                    if (inHeaderList(position) || inFooterList(position) || inEmptyList(position)) {
                        return manager.spanCount
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

    override fun isStickyHeader(position: Int): Boolean {
        //粒度由小到大
        //优先判断每一个item实例是否单独设置sticky
        val item = getItem(position)
        if (item is YasuoBaseItem) {
            if (item.sticky) {
                return true
            }
        }
        //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置sticky
        val itemConfig = itemClassTypes[item::class]
            ?: throw RuntimeException("找不到对应Config")
        if (itemConfig.sticky) {
            return true
        }
        return false
    }

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
     * empty列表改变的监听
     * Monitoring of footer list changes
     */
    val emptyListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
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
     * footer列表改变的监听
     * Monitoring of footer list changes
     */
    val footerListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(contributorViewModels: ObservableList<T>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            notifyItemRangeChanged(i + headerList.size + itemList.size + emptyList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerList.size + itemList.size + emptyList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerList.size + itemList.size + emptyList.size, i1 + headerList.size + itemList.size + emptyList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerList.size + itemList.size + emptyList.size, i1)
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
fun <T, VH, Config : YasuoBaseItemConfig<T, VH>, Adapter : YasuoBaseRVAdapter<T, VH, Config>, RV : RecyclerView> Adapter.attach(rv: RV): Adapter {
    rv.adapter = this
    return this
}
