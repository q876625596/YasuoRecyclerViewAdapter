package com.fusion_nex_gen.yasuorvadapter.listener

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.YasuoItemTouchHelper
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import java.util.*

/**
 * 设置item是否可拖拽、滑动删除
 */
fun <T : Any, RV : RecyclerView, VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<T, VH, *>> Adapter.enableDragOrSwipe(
    rv: RV,
    isLongPressDragEnable: Boolean = true,
    isItemViewSwipeEnable: Boolean = false,
    dragDirection: Int =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    swipeDirection: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    disableLayoutType: IntArray = intArrayOf(),
    onDisableDragOrSwipe: ((item: T, actionState: Int) -> Boolean)? = null,
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
            //拖拽/侧滑判断
            private fun judgeHolder(selected: RecyclerView.ViewHolder?, actionState: Int) {
                requireNotNull(selected) { "Must pass a ViewHolder when dragging" }
                //如果该holder是折叠模式
                if (itemIdTypes[selected.itemViewType]?.isFold == true) {
                    val item = getItem(selected.bindingAdapterPosition) as YasuoFoldItem
                    //如果选中的item已展开
                    if (item.isExpand) {
                        //可以实现监听，根据返回值判断是否可以继续拖拽或者侧滑
                        if (onDisableDragOrSwipe?.invoke(item as T, actionState) == true) {
                            return
                        }
                        //如果可以，那么先收起已经展开的子级
                        expandOrFoldItem(item)
                    }
                }
                /*      if (adapter.isFold) {
                          val position = selected.bindingAdapterPosition
                          val item = adapter.getItem(position) as YasuoFoldItem
                          //如果选中的item已展开
                          if (item.isExpand) {
                              //可以实现监听，根据返回值判断是否可以继续拖拽或者侧滑
                              if (onDisableDragOrSwipe?.invoke(item, actionState) == true) {
                                  return
                              }
                              //如果可以，那么先收起已经展开的子级
                              adapter.expandOrFoldItem(item)
                          }
                      }*/
                //如果是拖拽，为了防止ObservableList自带的notify影响，先移除itemList监听
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

class YasuoItemTouchHelperCallBack<VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH, *>>(
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


    //禁用默认的长按，使用 setOnLongClickListener来监听长按
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
        val fromItemConfig = adapter.itemIdTypes[viewHolder.itemViewType] ?: throw RuntimeException("找不到对应Config")
        val targetItemConfig = adapter.itemIdTypes[target.itemViewType] ?: throw RuntimeException("找不到对应Config")
        when {
            //如果两个都为折叠布局
            fromItemConfig.isFold && targetItemConfig.isFold -> {
                fromItem = adapter.getItem(fromPosition) as YasuoFoldItem
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果两个折叠布局不是同一个展开列表，或者不是同一级的情况，则不能交换
                if (fromItem.parentHash != targetItem.parentHash) {
                    return false
                }
                //如果目标item已经展开，也不能交换
                if (targetItem.isExpand) {
                    return false
                }
                //如果拖拽Holder的父节点不为空，说明此时拖拽的是子级
                if (fromItem.parentHash != null) {
                    //那么找到父级item，在下方使用
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
            targetItemConfig.isFold -> {
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果目标item是展开的，那么不能交换
                if (targetItem.isExpand) {
                    return false
                }
                //如果目标item是子级，那么也不能交换
                if (targetItem.parentHash != null) {
                    return false
                }
            }
        }
/*        if (adapter.itemIdTypes[viewHolder.itemViewType]?.isFold == true) {
            fromItem = adapter.getItem(fromPosition) as YasuoFoldItem
            //并且目标的Holder也是折叠布局（父级或子级）
            if (adapter.itemIdTypes[target.itemViewType]?.isFold == true) {
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果两个折叠布局不是同一个展开列表，或者不是同一级的情况，则不能交换
                if (fromItem.parentHash != targetItem.parentHash) {
                    return false
                }
                //如果目标item已经展开，也不能交换
                if (targetItem.isExpand) {
                    return false
                }
                //如果拖拽Holder的父节点不为空，说明此时拖拽的是子级
                if (fromItem.parentHash != null) {
                    //那么找到父级item，在下方使用
                    parentItem = adapter.itemList.find {
                        it.hashCode() == fromItem.parentHash
                    } as YasuoFoldItem
                }
            }
        }*/
        /* if (adapter.isFold) {
             fromItem = adapter.getItem(fromPosition) as YasuoFoldItem
             targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
             //如果不是同一个展开列表，或者不是同一级的情况，则不能交换
             if (fromItem.parentHash != targetItem.parentHash) {
                 return false
             }
             //如果目标item已经展开，也不能交换
             if (targetItem.isExpand) {
                 return false
             }
             //如果拖拽item的父节点不为空，那么找到父级item，在下方使用
             if (fromItem.parentHash != null) {
                 parentItem = adapter.itemList.find {
                     it.hashCode() == fromItem.parentHash
                 } as YasuoFoldItem
             }
         }*/
        //交换item
        if (fromPosition < targetPosition) {
            //如果是拖拽的Holder是折叠布局，并且父节点不为空
            if (fromItemConfig.isFold && parentItem != null) {
                for (i in fromPosition until targetPosition) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i + 1))
                    //那么交换当前子级列表中的item
                    Collections.swap(parentItem!!.list, adapter.getItemTruePosition(i) - parentItemIndex - 1, adapter.getItemTruePosition(i + 1) - parentItemIndex - 1)
                }
            } else {
                for (i in fromPosition until targetPosition) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i + 1))
                }
            }
        } else {
            //如果是拖拽的Holder是折叠布局，并且父节点不为空
            if (fromItemConfig.isFold && parentItem != null) {
                for (i in fromPosition downTo targetPosition + 1) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i - 1))
                    //那么交换当前子级列表中的item
                    Collections.swap(parentItem!!.list, adapter.getItemTruePosition(i) - parentItemIndex - 1, adapter.getItemTruePosition(i - 1) - parentItemIndex - 1)
                }
            } else {
                for (i in fromPosition downTo targetPosition + 1) {
                    Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i - 1))
                }
            }
        }
        adapter.notifyItemMoved(fromPosition, targetPosition)

/*        if (adapter.isFold && parentItem != null) {
            //那么交换当前子级列表中的item
            Collections.swap(parentItem.list, parentItem.list.indexOf(fromItem), parentItem.list.indexOf(targetItem))
        }*/
        innerDragListener?.invoke(adapter.getItemTruePosition(fromPosition), adapter.getItemTruePosition(targetPosition))
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val truePosition = adapter.getItemTruePosition(viewHolder.bindingAdapterPosition)
        val item = adapter.getItem(viewHolder.bindingAdapterPosition)
        //判断是否是折叠布局，如果是折叠布局，那么将折叠布局中的原始item也删除
        adapter.removeFoldChildListItem(item)
        innerSwipeListener?.invoke(truePosition, direction)
        adapter.itemList.removeAt(truePosition)
    }
}