package com.fusion_nex_gen.yasuorvadapter.listener

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.YasuoItemTouchHelper
import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem
import java.util.*

fun <T : Any, RV : RecyclerView, VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<T, VH, *>> YasuoItemTouchHelperCallBack<VH, Adapter>.attach(
    rv: RV,
    onDisableDragOrSwipe: ((item: T, actionState: Int) -> Boolean)? = null
): ItemTouchHelper {
    adapter.itemTouchHelper = object : YasuoItemTouchHelper(this) {
        //拖拽/侧滑判断
        private fun judgeHolder(selected: RecyclerView.ViewHolder?, actionState: Int) {
            requireNotNull(selected) { "Must pass a ViewHolder when dragging" }
            //如果该holder是折叠模式
            if (adapter.itemIdTypes[selected.itemViewType]?.isFold == true) {
                val item = adapter.getItem(selected.bindingAdapterPosition) as YasuoFoldItem
                //如果选中的item已展开
                if (item.isExpand) {
                    //可以实现监听，根据返回值判断是否可以继续拖拽或者侧滑
                    if (onDisableDragOrSwipe?.invoke(item as T, actionState) == true) {
                        return
                    }
                    //如果可以，那么先收起已经展开的子级
                    adapter.expandOrFoldItem(item)
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
                adapter.itemList.removeOnListChangedCallback(adapter.itemListListener)
            }
        }

        override fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState == ACTION_STATE_DRAG || actionState == ACTION_STATE_SWIPE) {
                judgeHolder(selected, actionState)
            }
            if (actionState == ACTION_STATE_IDLE) {
                adapter.itemList.addOnListChangedCallback(adapter.itemListListener)
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
    return adapter.itemTouchHelper!!
}

class YasuoItemTouchHelperCallBack<VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH, *>>(
    val adapter: Adapter,
    val isLongPressDragEnable: Boolean = true,
    val isItemViewSwipeEnable: Boolean = false,
    val disableLayoutType: IntArray = intArrayOf(),
    val innerDragListener: ((from: Int, target: Int) -> Unit)? = null,
    val innerSwipeListener: ((position: Int, direction: Int) -> Boolean)? = null
) : ItemTouchHelper.Callback() {
    var dragDirection: Int =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    var swipeDirection: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

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

        var fromItem: YasuoFoldItem? = null
        var targetItem: YasuoFoldItem? = null
        var parentItem: YasuoFoldItem? = null
        //如果拖动的Holder是折叠布局（父级或子级）
        val fromItemConfig = adapter.itemIdTypes[viewHolder.itemViewType] ?: throw RuntimeException("找不到对应Config")
        val targetItemConfig = adapter.itemIdTypes[target.itemViewType] ?: throw RuntimeException("找不到对应Config")
        when {
            fromItemConfig.isFold && targetItemConfig.isFold -> {
                fromItem = adapter.getItem(fromPosition) as YasuoFoldItem
                targetItem = adapter.getItem(targetPosition) as YasuoFoldItem
                //如果两个折叠布局不是同一个展开列表，或者不是同一级的情况，则不能交换
                Log.e("fromItem.parentHash",fromItem.parentHash.toString())
                Log.e("targetItem.parentHash",targetItem.parentHash.toString())
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
            for (i in fromPosition until targetPosition) {
                Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i + 1))
            }
        } else {
            for (i in fromPosition downTo targetPosition + 1) {
                Collections.swap(adapter.itemList, adapter.getItemTruePosition(i), adapter.getItemTruePosition(i - 1))
            }
        }
        adapter.notifyItemMoved(fromPosition, targetPosition)
        //如果是拖拽的Holder是折叠布局，并且父节点不为空
        if (adapter.itemIdTypes[viewHolder.itemViewType]?.isFold == true && parentItem != null) {
            //那么交换当前子级列表中的item
            Collections.swap(parentItem.list, parentItem.list.indexOf(fromItem), parentItem.list.indexOf(targetItem))
        }
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
        adapter.removeFoldListItem(item)
        innerSwipeListener?.invoke(truePosition, direction)
        adapter.itemList.removeAt(truePosition)
    }
}