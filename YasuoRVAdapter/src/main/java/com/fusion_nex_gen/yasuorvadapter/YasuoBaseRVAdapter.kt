package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fusion_nex_gen.yasuorvadapter.bean.*
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
abstract class YasuoBaseRVAdapter<T : Any, VH : RecyclerView.ViewHolder, Config : YasuoBaseItemConfig<T, VH>>
    (
    //item列表
    val itemList: YasuoList<T>,
    //头部列表
    val headerList: YasuoList<T>,
    //尾部列表
    val footerList: YasuoList<T>,
) :
    RecyclerView.Adapter<VH>(), StickyCallBack {

    internal val dataInvalidation = Any()

    /**
     * item列表布局配置的集合，实体类[KClass]作为key，类型[YasuoBaseItemConfig]作为value
     * 通常是这样获取：itemClassTypes[getItem(position)::class]
     */
    val itemClassTypes: MutableMap<KClass<*>, Config> = mutableMapOf()

    /**
     * item列表布局配置的集合，layoutId作为key，类型[YasuoBaseItemConfig]作为value
     * 相当于[itemClassTypes]的复制品，为的是能通过layoutId来获取[YasuoBaseItemConfig]
     * 并不会增加太多内存，但这样会减少一些常用操作的耗时
     * 通常是这样获取：itemIdTypes[getItemViewType]
     */
    val itemIdTypes: SparseArray<Config> = SparseArray()

    /**
     * RV
     */
    internal var recyclerView: RecyclerView? = null
    fun getRecyclerView() = recyclerView

    /**
     * 布局创建器
     */
    internal var inflater: LayoutInflater? = null
    fun initInflater(context: Context) {
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
    }

    /**
     * 拖拽/侧滑删除
     */
    internal var itemTouchHelper: ItemTouchHelper? = null

    /******空布局******/

    /**
     * emptyLayout列表
     * 该列表最多只会存在一个item
     * 使用列表方式储存是为了保持统一
     */
    val emptyList: YasuoList<T> = YasuoList()

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
    fun showEmptyLayout(emptyItem: T, clearHeader: Boolean = false, clearFooter: Boolean = false) {
        //先锁定loadMoreListener
        lockedLoadMoreListener = true
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
        if (emptyList.isEmpty()) {
            emptyList.add(emptyItem)
        } else {
            emptyList[0] = emptyItem
        }
    }

    /******加载更多******/

    /**
     * loadMoreLayout列表
     * 该列表最多只会存在一个item
     * 使用列表方式储存是为了保持统一
     */
    val loadMoreList: YasuoList<T> = YasuoList()

    /**
     * 锁定加载更多的监听，如果为true，那么不再触发监听
     */
    internal var lockedLoadMoreListener = false
    fun enableLoadMoreListener() {
        lockedLoadMoreListener = false
    }

    fun disableLoadMoreListener() {
        lockedLoadMoreListener = true
    }

    /**
     * 调用此方法手动刷新loadMore显示的内容
     */
    fun showLoadMoreLayout(loadMoreItem: T) {
        if (loadMoreList.isEmpty()) {
            loadMoreList.add(loadMoreItem)
        } else {
            loadMoreList[0] = loadMoreItem
        }
    }

    fun removeLoadMore() {
        loadMoreList.clear()
    }

    /******item相关******/

    /**
     * 获取可显示的所有的item数量
     */
    override fun getItemCount(): Int {
        //这里如果是显示空布局，那么就需要把loadMore隐藏
        return getAllListSize() - if (isShowEmptyLayout()) loadMoreList.size else 0
    }

    /**
     * 获取全部列表的长度
     */
    fun getAllListSize() = headerList.size + itemList.size + emptyList.size + footerList.size + loadMoreList.size

    /**
     * 如果item列表包含折叠布局
     * 那么需要通过此方法获取item列表的实际长度
     */
    fun getItemListTrueSize(): Int {
        var size = itemList.size
        itemList.forEach {
            if (it is YasuoFoldItem) {
                if (it.isExpand) {
                    size -= it.list.size
                    size = subFoldItemSize(size, it.list)
                }
            }
        }
        return size
    }

    /**
     * 如果header列表包含折叠布局
     * 那么需要通过此方法获取item列表的实际长度
     */
    fun getHeaderListTrueSize(): Int {
        var size = headerList.size
        headerList.forEach {
            if (it is YasuoFoldItem) {
                if (it.isExpand) {
                    size -= it.list.size
                    size = subFoldItemSize(size, it.list)
                }
            }
        }
        return size
    }

    /**
     * 如果footer列表包含折叠布局
     * 那么需要通过此方法获取item列表的实际长度
     */
    fun getFooterListTrueSize(): Int {
        var size = footerList.size
        footerList.forEach {
            if (it is YasuoFoldItem) {
                if (it.isExpand) {
                    size -= it.list.size
                    size = subFoldItemSize(size, it.list)
                }
            }
        }
        return size
    }

    private fun subFoldItemSize(size: Int, list: YasuoList<YasuoFoldItem>): Int {
        var newSize = size
        list.forEach {
            if (!it.isExpand) {
                //continue
                return@forEach
            }
            newSize -= it.list.size
            newSize = subFoldItemSize(newSize, it.list)
        }
        return newSize
    }

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
     * 获取loadMoreList的真实position
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getLoadMoreTruePosition(position: Int) = position - footerList.size - emptyList.size - headerList.size - itemList.size

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
        return position in headerList.size + itemList.size + emptyList.size until headerList.size + itemList.size + emptyList.size + footerList.size
    }

    /**
     * 判断position只包含在loadMoreList内
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inLoadMoreList(position: Int): Boolean {
        return position in headerList.size + itemList.size + emptyList.size + footerList.size until getAllListSize()
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
            inLoadMoreList(position) -> loadMoreList[getLoadMoreTruePosition(position)]
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
     * 是否全局都是折叠布局
     * [enableAllFold]方法需要在holderBind前调用
     * 设置该属性后，无需在每个holderBind中单独设置[YasuoBaseItemConfig.isFold]
     */
    internal var isAllFold: Boolean = false
    fun enableAllFold() {
        isAllFold = true
    }

    /**
     * 展开/折叠某个item
     * @param item 需要展开的item
     */
    fun expandOrFoldItem(item: YasuoFoldItem) {
        //如果下一级列表为 空，那么不做任何操作
        if (item.list.isEmpty()) {
            return
        }
        //如果已经展开，那么收起
        if (foldChild(item)) {
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
        //获取该item在itemList中的位置
        val position = itemList.indexOf(item as T)
        itemList.addAll(position + 1, item.list as YasuoList<T>)
    }

    /**
     * 将子级的展开折叠起来
     * @param item 折叠布局的item
     * @return 如果该布局已展开，那么返回true，反之返回false
     */
    private fun foldChild(item: YasuoFoldItem): Boolean {
        //先获取该item在itemList中的位置
        val position = itemList.indexOf(item as T)
        //如果已经展开，那么收起
        if (item.isExpand) {
            item.isExpand = false
            item.list.forEach {
                foldChild(it)
            }
            //从该item位置+1的地方开始，长度为该item子级列表的数量
            itemList.removeFrom(position + 1, position + 1 + item.list.size)
            return true
        }
        return false
    }

    /**
     * 如果有操作需要删除展开列表中的子级item
     * 则需要在list.remove之前调用此方法
     * @param childItem 需要删除的子级item
     */
    fun removeFoldChildListItem(childItem: Any) {
        //首先判断该item是否继承于FoldItem
        //如果不是继承于YasuoFoldItem，那么不做其他操作
        if (childItem !is YasuoFoldItem) {
            return
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
            //如果为折叠布局
            if (item is YasuoFoldItem) {
                //必须是第一级item才能被item.sticky影响而占满
                if (item.sticky && item.parentHash == null) {
                    lp.isFullSpan = true
                    return
                }
                //否则通过staggeredGridFullSpan判断
                if (item.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
                //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置staggeredGridFullSpan或sticky
                val itemConfig = itemIdTypes[holder.itemViewType]
                    ?: throw RuntimeException("找不到Config")
                //必须是第一级item才能被itemConfig.sticky影响而占满
                if (itemConfig.sticky && item.parentHash == null) {
                    lp.isFullSpan = true
                    return
                }
                if (itemConfig.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
            } else {//非折叠布局时
                //继承了基类，对实例单独设置
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
                //只有为非折叠布局时，isFullSpan才受到sticky影响
                if (itemConfig.sticky) {
                    lp.isFullSpan = true
                    return
                }
                if (itemConfig.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
            }
            //最后头部，尾部，空布局，加载更多布局默认占满
            lp.isFullSpan = inLoadMoreList(position) || inEmptyList(position) || inHeaderList(position) || inFooterList(position)
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
                    //如果为折叠布局
                    if (item is YasuoFoldItem) {
                        //必须是第一级item才能被item.sticky影响而占满
                        if (item.sticky && item.parentHash == null) {
                            return manager.spanCount
                        }
                        //否则根据gridSpan判断
                        if (item.gridSpan != 0) {
                            return item.gridSpan
                        }
                        //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置gridSpan或sticky
                        val itemConfig = itemClassTypes[item::class]
                            ?: throw RuntimeException("找不到对应Config")
                        //必须是第一级item才能被itemConfig.sticky影响而占满
                        if (itemConfig.sticky && item.parentHash == null) {
                            return manager.spanCount
                        }
                        //否则根据gridSpan判断
                        if (itemConfig.gridSpan != 0) {
                            return itemConfig.gridSpan
                        }
                    } else {//非折叠布局
                        //继承了基类，对实例单独设置
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
                        if (itemConfig.sticky) {
                            return manager.spanCount
                        }
                        if (itemConfig.gridSpan != 0) {
                            return itemConfig.gridSpan
                        }
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

    /**
     * 判断是否吸顶
     */
    override fun isStickyHeader(position: Int): Boolean {
        //粒度由小到大
        //优先判断每一个item实例是否单独设置sticky
        val item = getItem(position)
        //如果为折叠布局
        if (item is YasuoFoldItem) {
            //那么必须是第一级item才能吸顶
            if (item.sticky && item.parentHash == null) {
                return true
            }
            //如果没有对实例单独设置，那么再判断每个item的布局类型有没有设置sticky
            val itemConfig = itemClassTypes[item::class]
                ?: throw RuntimeException("找不到对应Config")
            //那么必须是第一级item才能吸顶
            if (itemConfig.sticky && item.parentHash == null) {
                return true
            }
        } else { //为非折叠布局时
            //继承了基类，对实例单独设置
            if (item is YasuoBaseItem) {
                //判断吸顶
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
            //当itemList插入行数据时，如果空布局正在显示，那么先隐藏空布局
            if (emptyList.isNotEmpty()) {
                emptyList.clear()
            }
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
     * loadMore列表改变的监听
     * Monitoring of footer list changes
     */
    val loadMoreListListener = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {
        override fun onChanged(contributorViewModels: ObservableList<T>) {
            notifyDataSetChanged()
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeChanged(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            notifyItemRangeChanged(i + headerList.size + itemList.size + emptyList.size + footerList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeInserted(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int
        ) {
            notifyItemRangeInserted(i + headerList.size + itemList.size + emptyList.size + footerList.size, i1)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeMoved(
            contributorViewModels: ObservableList<T>,
            i: Int,
            i1: Int,
            i2: Int
        ) {
            notifyItemMoved(i + headerList.size + itemList.size + emptyList.size + footerList.size, i1 + headerList.size + itemList.size + emptyList.size + footerList.size)
            afterDataChangeListener?.invoke()
        }

        override fun onItemRangeRemoved(contributorViewModels: ObservableList<T>, i: Int, i1: Int) {
            if (contributorViewModels.isEmpty()) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeRemoved(i + headerList.size + itemList.size + emptyList.size + footerList.size, i1)
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
