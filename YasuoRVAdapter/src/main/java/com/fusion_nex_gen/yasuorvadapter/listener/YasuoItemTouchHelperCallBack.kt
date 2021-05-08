package com.fusion_nex_gen.yasuorvadapter.listener

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.YasuoItemTouchHelper
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import java.util.*

/**
 * 设置item是否可拖拽、滑动删除
 * Set whether the item can be dragged or deleted by sliding
 */
fun <RV : RecyclerView, VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<VH, *>> Adapter.enableDragOrSwipe(
    rv: RV,
    isLongPressDragEnable: Boolean = true,
    isItemViewSwipeEnable: Boolean = false,
    dragDirection: Int =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    swipeDirection: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    disableLayoutType: IntArray = intArrayOf(),
    onDisableDragOrSwipe: ((item: Any, actionState: Int) -> Boolean)? = null,
    innerDragListener: ((from: Int, target: Int) -> Unit)? = null,
    innerSwipeListener: ((position: Int, direction: Int) -> Boolean)? = null,
): ItemTouchHelper {
    itemTouchHelper =
        object : YasuoItemTouchHelper(
            YasuoItemTouchHelperCallBack(
                this,
                isLongPressDragEnable,
                isItemViewSwipeEnable,
                dragDirection,
                swipeDirection,
                disableLayoutType,
                innerDragListener,
                innerSwipeListener
            )
        ) {
            /**
             * 拖拽/侧滑判断
             * Drag / sideslip judgment
             */
            private fun judgeHolder(selected: RecyclerView.ViewHolder?, actionState: Int) {
                requireNotNull(selected) { "Must pass a ViewHolder when dragging" }
                //如果该holder是折叠模式
                //If the holder is in collapse mode
                if (itemIdTypes[selected.itemViewType]?.isFold == true) {
                    val item = getItem(selected.bindingAdapterPosition) as YasuoFoldItem
                    //如果选中的item已展开
                    //If the selected item is expanded
                    if (item.isExpand) {
                        //可以实现监听，根据返回值判断是否可以继续拖拽或者侧滑
                        //Can monitor, according to the return value to determine whether you can continue to drag or sideslip
                        if (onDisableDragOrSwipe?.invoke(item, actionState) == true) {
                            return
                        }
                        //如果可以，那么先收起已经展开的子级
                        //If you can, collapse the expanded children first
                        expandOrFoldItem(item)
                    }
                }
                //如果是拖拽，为了防止ObservableList自带的notify影响，先移除itemList监听
                //In case of dragging, in order to prevent the notify effect of ObservableList, first remove the itemList listener
                if (actionState == ACTION_STATE_DRAG) {
                    itemList.removeOnListChangedCallback(itemListListener)
                }
            }

            override fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
                if (actionState == ACTION_STATE_DRAG || actionState == ACTION_STATE_SWIPE) {
                    judgeHolder(selected, actionState)
                }
                if (actionState == ACTION_STATE_IDLE) {
                    itemList.addOnListChangedCallback(itemListListener)
                }
                super.select(selected, actionState)
            }

            override fun startDrag(viewHolder: RecyclerView.ViewHolder) {
                judgeHolder(viewHolder, ACTION_STATE_DRAG)
                super.startDrag(viewHolder)
            }

            override fun startSwipe(viewHolder: RecyclerView.ViewHolder) {
                judgeHolder(viewHolder, ACTION_STATE_SWIPE)
                super.startSwipe(viewHolder)
            }
        }.apply {
            attachToRecyclerView(rv)
        }
    return itemTouchHelper!!
}

class YasuoItemTouchHelperCallBack<VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<VH, *>>(
    val adapter: Adapter,
    val isLongPressDragEnable: Boolean = true,
    val isItemViewSwipeEnable: Boolean = false,
    val dragDirection: Int =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    val swipeDirection: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    val disableLayoutType: IntArray = intArrayOf(),
    val innerDragListener: ((from: Int, target: Int) -> Unit)? = null,
    val innerSwipeListener: ((position: Int, direction: Int) -> Boolean)? = null
) : ItemTouchHelper.Callback() {

    override fun isLongPressDragEnabled(): Boolean = isLongPressDragEnable

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnable

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (disableLayoutType.contains(viewHolder.itemViewType) || !adapter.inItemList(viewHolder.bindingAdapterPosition)) {
            return makeMovementFlags(0, 0)
        }
        return makeMovementFlags(dragDirection, swipeDirection)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        if (disableLayoutType.contains(viewHolder.itemViewType) || !adapter.inItemList(viewHolder.bindingAdapterPosition) || !adapter.inItemList(target.bindingAdapterPosition)) {
            return false
        }
        val fromPosition = viewHolder.bindingAdapterPosition
        val targetPosition = target.bindingAdapterPosition

        val fromItem: YasuoFoldItem?
        val targetItem: YasuoFoldItem?
        var parentItem: YasuoFoldItem? = null
        var parentItemIndex = 0
        //如果拖动的Holder是折叠布局（父级或子级）
        //If the dragged holder is a fold layout (parent or child)
        val fromItemConfig = adapter.itemIdTypes[viewHolder.itemViewType] ?: throw RuntimeException("找不到对应Config")
        val targetItemConfig = adapter.itemIdTypes[target.itemViewType] ?: throw RuntimeException("找不到对应Config")
        when {
            //如果两个都为折叠布局
            //If both are fold layout
            fromItemConfig.isFold && targetItemConfig.isFold -> {
                fromItem = adapter.getItem(fromPosition) as YasuoFoldItem
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果两个折叠布局不是同一个展开列表，或者不是同一级的情况，则不能交换
                //If two fold layouts are not the same expanded list or the same level, they cannot be exchanged
                if (fromItem.parentHash != targetItem.parentHash) {
                    return false
                }
                //如果目标item已经展开，也不能交换
                //If the target item has been expanded, it cannot be exchanged
                if (targetItem.isExpand) {
                    return false
                }
                //如果拖拽Holder的父节点不为空，说明此时拖拽的是子级
                //If the parent node of the drag holder is not empty, it means that the child is being dragged
                if (fromItem.parentHash != null) {
                    //那么找到父级item，在下方使用
                    //Then find the parent item and use it below
                    run out@{
                        adapter.itemList.forEachIndexed { index, any ->
                            if (any.hashCode() == fromItem.parentHash) {
                                parentItem = any as YasuoFoldItem
                                parentItemIndex = index
                                //break
                                return@out
                            }
                        }
                    }
                }
            }
            //如果只有目标Holder是折叠布局
            //If only the target holder is fold layout
            targetItemConfig.isFold -> {
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果目标item是展开的，那么不能交换
                //If the target item is expanded, it cannot be exchanged
                if (targetItem.isExpand) {
                    return false
                }
                //如果目标item是子级，那么也不能交换
                //If the target item is a child, it cannot be exchanged
                if (targetItem.parentHash != null) {
                    return false
                }
            }
        }
        //交换item
        //Exchange item
        if (fromPosition < targetPosition) {
            //如果是拖拽的Holder是折叠布局，并且父节点不为空
            //If the holder is dragged, the layout is fold and the parent node is not empty
            if (fromItemConfig.isFold && parentItem != null) {
                for (i in fromPosition until targetPosition) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i + 1))
                    //那么交换当前子级列表中的item
                    //Then exchange the items in the current child list
                    Collections.swap(parentItem!!.list, adapter.getItemTruePosition(i) - parentItemIndex - 1, adapter.getItemTruePosition(i + 1) - parentItemIndex - 1)
                }
            } else {
                for (i in fromPosition until targetPosition) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i + 1))
                }
            }
        } else {
            //如果是拖拽的Holder是折叠布局，并且父节点不为空
            //If the holder is dragged, the layout is collapsed and the parent node is not empty
            if (fromItemConfig.isFold && parentItem != null) {
                for (i in fromPosition downTo targetPosition + 1) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i - 1))
                    //那么交换当前子级列表中的item
                    //Then exchange the items in the current child list
                    Collections.swap(parentItem!!.list, adapter.getItemTruePosition(i) - parentItemIndex - 1, adapter.getItemTruePosition(i - 1) - parentItemIndex - 1)
                }
            } else {
                for (i in fromPosition downTo targetPosition + 1) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i - 1))
                }
            }
        }
        adapter.notifyItemMoved(fromPosition, targetPosition)
        innerDragListener?.invoke(adapter.getItemTruePosition(fromPosition), adapter.getItemTruePosition(targetPosition))
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val truePosition = adapter.getItemTruePosition(viewHolder.bindingAdapterPosition)
        val item = adapter.getItem(viewHolder.bindingAdapterPosition)
        innerSwipeListener?.invoke(truePosition, direction)
        //判断是否是折叠布局，如果是折叠布局，那么将折叠布局中的原始item也删除
        //Judge whether it is a folded layout. If it is a folded layout, the original items in the folded layout will also be deleted
        adapter.removeAndFoldListItem(item)
    }
}