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
 * 2、空白页/头部/尾部
 * 3、加载更多
 * 4、折叠布局（支持多级折叠）
 * 5、拖拽、横向滑动删除
 * 6、动画的高可配置(采用recyclerView的itemAnimator方案，详见mikepenz/ItemAnimators库)
 * 7、吸顶(采用qiujayen/sticky-layoutmanager的方案，低耦合adapter和item，最低限度的修改代码)
 * 8、附送两个ItemDecoration，可根据不同需求选择
 * TODO 9、横向滑动显示选项
 * TODO 10、新增一些额外的常用功能，比如结合下拉刷新之后显示一个一临时头部，提示刷新了多少条
 */
abstract class YasuoBaseRVAdapter<T : Any, VH : RecyclerView.ViewHolder, Config : YasuoBaseItemConfig<T, VH>>
    (
    /**
     * item列表
     * Item list
     */
    val itemList: YasuoList<T>,
    /**
     * 头部列表
     * Header list
     */
    val headerList: YasuoList<T>,
    /**
     * 尾部列表
     * Footer list
     */
    val footerList: YasuoList<T>,
) :
    RecyclerView.Adapter<VH>(), StickyCallBack {

    internal val dataInvalidation = Any()

    /**
     * 列表布局配置的集合，实体类[KClass]作为key，类型[YasuoBaseItemConfig]作为value
     * 通常是这样获取：itemClassTypes[getItem(position)::class]
     * Set of list layout configurations, with entity class [KClass] as key and type [YasuoBaseItemConfig] as value
     * Usually, get: itemClassTypes[getItem (position)::class]
     */
    val itemClassTypes: MutableMap<KClass<*>, Config> = mutableMapOf()

    /**
     * 列表布局配置的集合，layoutId作为key，类型[YasuoBaseItemConfig]作为value
     * 相当于[itemClassTypes]的复制品，为的是能通过layoutId来获取[YasuoBaseItemConfig]
     * 这样做是为了更方便的获取到[YasuoBaseItemConfig]
     * 通常是这样获取：itemIdTypes[getItemViewType]
     * A set of list layout configurations, with layoutId as key and type [YasuoBaseItemConfig] as value
     * It is equivalent to a replica of [itemClassTypes] so that [YasuoBaseItemConfig] can be obtained through layoutId
     * This is to obtain [YasuoBaseItemConfig] more conveniently
     * Usually, get: itemClassTypes[getItem (position)::class]
     */
    val itemIdTypes: SparseArray<Config> = SparseArray()

    /**
     * RecyclerView
     */
    internal var recyclerView: RecyclerView? = null
    fun getRecyclerView() = recyclerView

    /**
     * 布局创建器
     * Layout Creator
     */
    internal var inflater: LayoutInflater? = null
    fun initInflater(context: Context) {
        if (inflater == null) {
            inflater = LayoutInflater.from(context)
        }
    }

    /**
     * 拖拽/侧滑删除
     * Drag / sideslip delete
     */
    internal var itemTouchHelper: ItemTouchHelper? = null

    /****** 空布局 Empty layout ******/

    /**
     * emptyLayout列表
     * 该列表最多只会存在一个item
     * 使用列表方式储存是为了保持统一
     * EmptyLayout list
     * There will be at most one item in the list
     * The use of list storage is to maintain uniformity
     */
    val emptyList: YasuoList<T> = YasuoList()

    /**
     * 判断当前是否是显示空布局状态
     * Determine whether the current display is empty layout state
     */
    fun isShowEmptyLayout(): Boolean {
        // 当itemList为空，并且已经设置emptyList不为空时
        // 表示此时页面为显示全屏布局
        // When itemList is empty and emptyList has been set not to be empty
        // It means that the page is in full screen layout
        return itemList.isNullOrEmpty() && emptyList.isNotEmpty()
    }

    /**
     * 设置空布局，该方法会强制将[itemList]清空，
     * 并将loadMore的监听锁定，[lockedLoadMoreListener] = true
     * Set an empty layout, which forces the [itemList] to be empty,
     * And lock the monitoring of loadMore, [lockedLoadMoreListener] = true
     */
    fun showEmptyLayout(emptyItem: T, clearHeader: Boolean = false, clearFooter: Boolean = false) {
        //先锁定loadMoreListener
        //Lock loadMoreListener first
        lockedLoadMoreListener = true
        //这一步设置很重要
        //如果布局是StaggeredGridLayoutManager，并且当此时列表触底的时候
        //在列表顶部会出现空白，因此先让列表回滚1像素
        //This step is very important
        //If the layout is staged grid layout manager, and the list hits the bottom at this time
        //White space will appear at the top of the list, so roll back the list by 1 pixel first
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

    /****** 加载更多 LoadMore******/

    /**
     * loadMoreLayout列表
     * 该列表最多只会存在一个item
     * 使用列表方式储存是为了保持统一
     * loadMoreLayout list
     * There will be at most one item in the list
     * The use of list storage is to maintain uniformity
     */
    val loadMoreList: YasuoList<T> = YasuoList()

    /**
     * 锁定加载更多的监听，如果为true，那么不再触发监听
     * Lock to load more listeners. If true, no more listeners will be triggered
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
     * Call this method to refresh the content displayed by loadMore manually
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

    /******item相关 Item related******/

    /**
     * 获取可显示的所有的item数量
     * Get the number of all items that can be displayed
     */
    override fun getItemCount(): Int {
        //这里如果是显示空布局，那么就需要把loadMore隐藏
        //If the empty layout is displayed, you need to hide loadMore
        return getAllListSize() - if (isShowEmptyLayout()) loadMoreList.size else 0
    }

    /**
     * 获取全部列表的长度
     * Gets the length of all lists
     */
    fun getAllListSize() = headerList.size + itemList.size + emptyList.size + footerList.size + loadMoreList.size

    /**
     * 如果[itemList]包含折叠布局
     * 那么需要通过此方法获取[itemList]的实际长度
     * If the [itemList] contains a Foldd layout
     * Then you need to get the actual length of the [itemList] through this method
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
     * 如果[headerList]包含折叠布局
     * 那么需要通过此方法获取[headerList]实际长度
     * If the [headerList] contains a Foldd layout
     * Then you need to get the actual length of the [headerList] through this method
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
     * 如果[footerList]包含折叠布局
     * 那么需要通过此方法获取[footerList]的实际长度
     * If the [footerList] contains a Foldd layout
     * Then you need to get the actual length of the [footerList] through this method
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

    /**
     * 减去子级已展开的列表长度
     * Subtract the length of expanded lists of children
     * @param size 真实的长度
     * Real length
     * @param list 子级列表
     * Child list
     */
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
     * 获取[headerList]的真实position
     * Get the real position of the [headerList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getHeaderTruePosition(position: Int) = position

    /**
     * 获取[itemList]的真实position
     * Get the real position of the [itemList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getItemTruePosition(position: Int) = position - headerList.size

    /**
     * 获取[emptyList]的真实position
     * Get the real position of the [emptyList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getEmptyTruePosition(position: Int) = position - headerList.size - itemList.size

    /**
     * 获取[footerList]的真实position
     * Get the real position of the [footerList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getFooterTruePosition(position: Int) = position - emptyList.size - headerList.size - itemList.size

    /**
     * 获取[loadMoreList]的真实position
     * Get the real position of the [loadMoreList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun getLoadMoreTruePosition(position: Int) = position - footerList.size - emptyList.size - headerList.size - itemList.size

    /**
     * 判断position只包含在[headerList]内
     * Judge that position is contained in [headerList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inHeaderList(position: Int): Boolean {
        return position in 0 until headerList.size
    }

    /**
     * 判断position只包含在[itemList]内
     * Judge that position is contained in [itemList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inItemList(position: Int): Boolean {
        return position in headerList.size until itemList.size + headerList.size
    }

    /**
     * 判断position只包含在[emptyList]内
     * Judge that position is contained in [emptyList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inEmptyList(position: Int): Boolean {
        return position in headerList.size + itemList.size until headerList.size + itemList.size + emptyList.size
    }

    /**
     * 判断position只包含在[footerList]内
     * Judge that position is contained in [footerList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inFooterList(position: Int): Boolean {
        return position in headerList.size + itemList.size + emptyList.size until headerList.size + itemList.size + emptyList.size + footerList.size
    }

    /**
     * 判断position只包含在[loadMoreList]内
     * Judge that position is contained in [loadMoreList]
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    fun inLoadMoreList(position: Int): Boolean {
        return position in headerList.size + itemList.size + emptyList.size + footerList.size until getAllListSize()
    }


    /**
     * 通过[position]获取item
     * Get item through [position]
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
                        "The position cannot find a corresponding entity,position = ${position}."
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
     * 通过[position]获取item的类型（类型即为布局id）
     * Get the type of item through [position] (the type is layout ID)
     * @param position [RecyclerView.ViewHolder.getBindingAdapterPosition]
     */
    override fun getItemViewType(position: Int): Int {
        return itemClassTypes[getItem(position)::class]?.itemLayoutId
            ?: throw RuntimeException(
                "找不到viewType，position = ${position}，\n" +
                        "viewType not found,position = $position"
            )
    }


    /****** 折叠布局  Fold layout ******/

    /**
     * 展开/折叠某个item
     * Expand/Fold an item
     * @param item 需要展开的item
     * Items to expand
     */
    fun expandOrFoldItem(item: YasuoFoldItem) {
        //如果下一级列表为空，那么不做任何操作
        //If the next level list is empty, no operation will be done
        if (item.list.isEmpty()) {
            return
        }
        //如果已经展开，那么收起
        //If it's already unfolded, fold it up
        if (foldChild(item)) {
            return
        }
        //如果未展开，那么展开
        //If not, expand
        item.isExpand = true
        //展开的同时给子级的parentHash赋值
        //At the same time, the parenthash of the child is assigned
        item.list.forEach {
            if (it.parentHash == null) {
                it.parentHash = item.hashCode()
            }
        }
        //获取该item在itemList中的位置
        //Gets the position of the item in the itemList
        val position = itemList.indexOf(item as T)
        itemList.addAll(position + 1, item.list as YasuoList<T>)
    }

    /**
     * 将子级的展开折叠起来
     * Fold the child's expansion
     * @param item 折叠布局的item，
     * Fold layout item
     * @return 如果该布局已展开，那么返回true，反之返回false
     * If the layout has been expanded, return true, otherwise return false
     */
    private fun foldChild(item: YasuoFoldItem): Boolean {
        //先获取该item在itemList中的位置
        //First get the position of the item in the itemList
        val position = itemList.indexOf(item as T)
        //如果已经展开，那么收起
        //If it's already unfolded, fold it up
        if (item.isExpand) {
            item.isExpand = false
            item.list.forEach {
                foldChild(it)
            }
            //从该item位置+1的地方开始，数量为该item子级列表的长度
            //Starting from the position + 1 of the item, the quantity is the length of the item child list
            itemList.removeFrom(position + 1, position + 1 + item.list.size)
            return true
        }
        return false
    }

    /**
     * 如果需要删除展开列表中的子级item
     * If you need to delete the child items in the expanded list
     * 则需要在[YasuoList.remove]之前调用此方法
     * You need to [YasuoList.remove] Before calling this method
     * @param childItem 需要删除的子级item
     * Child items to be deleted
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
        //判断StaggeredGridLayoutManager的item是否占满
        //Judge whether the items of StaggeredGridLayoutManager are full span
        val lp = holder.itemView.layoutParams
        if (lp != null && lp is StaggeredGridLayoutManager.LayoutParams) {
            //判断粒度由小到大，优先级sticky > staggeredGridFullSpan
            //The judgment granularity is from small to large, and the priority is sticky > staggeredGridFullSpan
            val position = holder.bindingAdapterPosition
            val item = getItem(position)
            //优先判断每一个item实例是否单独设置staggeredGridFullSpan或sticky
            //Priority is given to judge whether each item instance is set separately as staggeredGridFullSpan or sticky
            //如果为折叠布局
            //If it is a folded layout
            if (item is YasuoFoldItem) {
                //必须是父级item才能使用item.sticky占满
                //Must be a parent item to use item.sticky set Full Span
                if (item.sticky && item.parentHash == null) {
                    lp.isFullSpan = true
                    return
                }
                //否则通过staggeredGridFullSpan判断
                //Otherwise, it can be judged by staggedgridfullspan
                if (item.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
                //如果没有对实例单独设置，那么再判断布局类型有没有设置staggeredGridFullSpan或sticky
                //If the instance is not set separately, then judge whether the layout type has set staggeredGridFullSpan or sticky
                val itemConfig = itemIdTypes[holder.itemViewType]
                    ?: throw RuntimeException(
                        "找不到对应的Config，position = ${holder.bindingAdapterPosition}\n" +
                                "No corresponding config was found，position = ${holder.bindingAdapterPosition}."
                    )
                //必须是父级item才能使用itemConfig.sticky占满
                //Must be a parent item to use itemConfig.sticky set Full Span
                if (itemConfig.sticky && item.parentHash == null) {
                    lp.isFullSpan = true
                    return
                }
                if (itemConfig.staggeredGridFullSpan) {
                    lp.isFullSpan = true
                    return
                }
            } else {
                //非折叠布局时
                //When non folding layout
                //如果继承了基类，对实例单独设置
                //If you inherit the base class, set the
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
                //如果没有对实例单独设置，那么再判断布局类型有没有设置staggeredGridFullSpan或sticky
                //If the instance is not set separately, then judge whether the layout type has set staggeredGridFullSpan or sticky
                val itemConfig = itemIdTypes[holder.itemViewType]
                    ?: throw RuntimeException(
                        "找不到对应的Config，position = ${holder.bindingAdapterPosition}\n" +
                                "No corresponding config was found，position = ${holder.bindingAdapterPosition}."
                    )
                //只有为非折叠布局时，isFullSpan才受到sticky影响
                //isFullSpan is only affected by sticky if it is a non folded layout
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
            //Finally, header, footer, emptyLayout, loadMoreLayout, full by default
            lp.isFullSpan = inLoadMoreList(position) || inEmptyList(position) || inHeaderList(position) || inFooterList(position)
        }
    }

    /**
     * 绑定到recyclerView
     * Bind to recyclerview
     */
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    //粒度由小到大，优先级sticky > gridSpan
                    //From small to large, the priority is sticky > gridSpan
                    //优先判断每一个item实例是否单独设置gridSpan或sticky
                    //Priority is given to judge whether gridSpan or sticky is set separately for each item instance
                    val item = getItem(position)
                    //如果为折叠布局
                    //If it is a folded layout
                    if (item is YasuoFoldItem) {
                        //必须是父级item才能使用item.sticky占满
                        //Must be a parent item to use item.sticky set Full Span
                        if (item.sticky && item.parentHash == null) {
                            return manager.spanCount
                        }
                        //否则根据gridSpan判断
                        //Otherwise, judge according to gridSpan
                        if (item.gridSpan != 0) {
                            return item.gridSpan
                        }
                        //如果没有对实例单独设置，那么再判断布局类型有没有设置gridSpan或sticky
                        //If the instance is not set separately, then judge whether the layout type has set gridSpan or sticky
                        val itemConfig = itemClassTypes[item::class]
                            ?: throw RuntimeException(
                                "找不到对应的Config，position = ${position}\n" +
                                        "No corresponding config was found，position = ${position}."
                            )
                        //必须是父级item才能使用itemConfig.sticky占满
                        //Must be a parent item to use itemConfig.sticky set Full Span
                        if (itemConfig.sticky && item.parentHash == null) {
                            return manager.spanCount
                        }
                        //否则根据gridSpan判断
                        //Otherwise, judge according to gridSpan
                        if (itemConfig.gridSpan != 0) {
                            return itemConfig.gridSpan
                        }
                    } else {
                        //非折叠布局时
                        //When non folding layout
                        //如果继承了基类，对实例单独设置
                        //If you inherit the base class, set the
                        if (item is YasuoBaseItem) {
                            if (item.sticky) {
                                return manager.spanCount
                            }
                            if (item.gridSpan != 0) {
                                return item.gridSpan
                            }
                        }
                        //如果没有对实例单独设置，那么再判断布局类型有没有设置gridSpan或sticky
                        //If the instance is not set separately, then judge whether the layout type has set gridSpan or sticky
                        val itemConfig = itemClassTypes[item::class]
                            ?: throw RuntimeException(
                                "找不到对应的Config，position = ${position}\n" +
                                        "No corresponding config was found，position = ${position}."
                            )
                        if (itemConfig.sticky) {
                            return manager.spanCount
                        }
                        if (itemConfig.gridSpan != 0) {
                            return itemConfig.gridSpan
                        }
                    }
                    //最后头部，尾部，空布局，加载更多布局默认占满
                    //Finally, header, footer, emptyLayout, loadMoreLayout, full by default
                    if (inHeaderList(position) || inFooterList(position) || inEmptyList(position) || inLoadMoreList(position)) {
                        return manager.spanCount
                    }
                    //如果以上都不是，默认占满1格
                    //If none of the above is true, 1 span will be filled by default
                    return 1
                }
            }
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    /**
     * 判断是否吸顶
     * Judge whether it's sticky or not
     */
    override fun isStickyHeader(position: Int): Boolean {
        //粒度由小到大
        //Grain size from small to large
        //优先判断每一个item实例是否单独设置sticky
        //Priority is given to judge whether sticky is set separately for each item instance
        val item = getItem(position)
        //如果为折叠布局
        //If it is a folded layout
        if (item is YasuoFoldItem) {
            //那么必须是父级item才能吸顶
            //Then it must be a parent item to be sticky
            if (item.sticky && item.parentHash == null) {
                return true
            }
            //如果没有对实例单独设置，那么再判断布局类型有没有设置sticky
            //If the instance is not set separately, then judge whether the layout type is set sticky
            val itemConfig = itemClassTypes[item::class]
                ?: throw RuntimeException(
                    "找不到对应的Config，position = ${position}\n" +
                            "No corresponding config was found，position = ${position}."
                )
            //那么必须是父级item才能吸顶
            //Then it must be a parent item to be sticky
            if (itemConfig.sticky && item.parentHash == null) {
                return true
            }
        } else {
            //非折叠布局时
            //When non folding layout
            //如果继承了基类，对实例单独设置
            //If you inherit the base class, set the
            if (item is YasuoBaseItem) {
                //判断吸顶
                //Judge sticky
                if (item.sticky) {
                    return true
                }
            }
            //如果没有对实例单独设置，那么再判断布局类型有没有设置sticky
            //If the instance is not set separately, then judge whether the layout type is set sticky
            val itemConfig = itemClassTypes[item::class]
                ?: throw RuntimeException(
                    "找不到对应的Config，position = ${position}\n" +
                            "No corresponding config was found，position = ${position}."
                )
            if (itemConfig.sticky) {
                return true
            }
        }
        return false
    }

    /****** 列表改变的监听 Monitoring of list changes ******/

    /**
     * [headerList]改变的监听
     * Monitoring of [headerList] changes
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
     * [itemList]改变的监听
     * Monitoring of [itemList] changes
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
     * [emptyList]改变的监听
     * Monitoring of [emptyList] changes
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
     * [footerList]改变的监听
     * Monitoring of [footerList] changes
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
     * [loadMoreList]改变的监听
     * Monitoring of [loadMoreList] changes
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
 * Bind Adapter
 */
fun <T, VH, Config : YasuoBaseItemConfig<T, VH>, Adapter : YasuoBaseRVAdapter<T, VH, Config>, RV : RecyclerView> Adapter.attach(rv: RV): Adapter {
    rv.adapter = this
    return this
}
