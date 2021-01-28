package com.fusion_nex_gen.yasuorvadapter

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.YasuoItemTouchHelper
import java.util.*

fun <RV : RecyclerView, VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH>> ItemTouchHelperCallBack<VH, Adapter>.attach(
    rv: RV
): ItemTouchHelper {
    adapter.itemTouchHelper = object : YasuoItemTouchHelper(this) {
        override fun select(selected: RecyclerView.ViewHolder?, actionState: Int) {
            if (actionState == ACTION_STATE_DRAG) {
                requireNotNull(selected) { "Must pass a ViewHolder when dragging" }
                adapter.itemList.removeOnListChangedCallback(adapter.itemListListener)
            }
            super.select(selected, actionState)
        }

        override fun startDrag(viewHolder: RecyclerView.ViewHolder) {
            adapter.itemList.removeOnListChangedCallback(adapter.itemListListener)
            super.startDrag(viewHolder)
        }
    }.apply {
        attachToRecyclerView(rv)
    }
    return adapter.itemTouchHelper!!
}

class ItemTouchHelperCallBack<VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH>>(
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

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        adapter.itemList.addOnListChangedCallback(adapter.itemListListener)
        super.clearView(recyclerView, viewHolder)
    }

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
        //adapter.notifyItemRangeChanged(min(fromPosition, targetPosition), abs(fromPosition - targetPosition) +1)
        innerDragListener?.invoke(adapter.getItemTruePosition(fromPosition), adapter.getItemTruePosition(targetPosition))
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val truePosition = adapter.getItemTruePosition(viewHolder.bindingAdapterPosition)
        val item = adapter.getItem(viewHolder.bindingAdapterPosition)
        Log.e("onSwiped.truePosition",truePosition.toString())
        Log.e("onSwiped.item",item::class.toString())
        //判断是否是折叠布局，如果是折叠布局，那么将折叠布局中的原始item也删除
        adapter.removeFoldListItem(item)
        innerSwipeListener?.invoke(truePosition, direction)
        adapter.itemList.removeAt(truePosition)
    }
}