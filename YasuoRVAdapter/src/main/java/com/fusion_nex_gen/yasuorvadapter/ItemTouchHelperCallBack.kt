package com.fusion_nex_gen.yasuorvadapter

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener
import java.util.*

fun <RV : RecyclerView, VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH>> ItemTouchHelperCallBack<VH, Adapter>.attach(
    rv: RV
): ItemTouchHelper {
    adapter.itemTouchHelper = object : ItemTouchHelper(this) {
        override fun startDrag(viewHolder: RecyclerView.ViewHolder) {
            if (adapter.itemList is ObList) {
                (adapter.itemList as ObList).removeOnListChangedCallback(adapter.itemListListener)
            }
            super.startDrag(viewHolder)
        }
    }.apply {
        attachToRecyclerView(rv)
    }
    //如果启用长按拖拽
    if (this.isLongPressDragEnable) {
        adapter.setGlobalItemHolderListener { holder ->
            holder.itemView.setOnLongClickListener {
                adapter.itemTouchHelper?.startDrag(holder)
                return@setOnLongClickListener true
            }
        }
    }
    return adapter.itemTouchHelper!!
}

class ItemTouchHelperCallBack<VH : RecyclerView.ViewHolder, Adapter : YasuoBaseRVAdapter<*, VH>>(
    val adapter: Adapter,
    val isLongPressDragEnable: Boolean = true,
    val isItemViewSwipeEnable: Boolean = false,
    val disableLayoutType: IntArray = intArrayOf()
) : ItemTouchHelper.Callback() {
    var dragDirection: Int =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    var swipeDirection: Int = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    private var innerDragListener: ItemDragListener<VH>? = null
    private var innerSwipeListener: ItemSwipeListener<VH>? = null

    //禁用默认的长按，使用 setOnLongClickListener来监听长按
    override fun isLongPressDragEnabled(): Boolean = false

    override fun isItemViewSwipeEnabled(): Boolean = isItemViewSwipeEnable

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (adapter.itemList is ObList) {
            (adapter.itemList as ObList).addOnListChangedCallback(adapter.itemListListener)
        }
        super.clearView(recyclerView, viewHolder)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (disableLayoutType.contains(viewHolder.itemViewType) || !adapter.inItemList(viewHolder.bindingAdapterPosition)) {
            return makeMovementFlags(0, 0)
        }
        Log.e("qqq",viewHolder.bindingAdapterPosition.toString())
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
        Log.e("aaa",fromPosition.toString())
        Log.e("bbb",targetPosition.toString())
        //TODO 位置错误
        if (adapter.inItemList(fromPosition) && adapter.inItemList(targetPosition)) {
            if (fromPosition < targetPosition) {
                for (i in fromPosition until targetPosition) {
                    Collections.swap(adapter.itemList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo targetPosition + 1) {
                    Collections.swap(adapter.itemList, i, i - 1)
                }
            }
            adapter.notifyItemMoved(fromPosition, targetPosition)
        }
        //adapter.notifyItemRangeChanged(min(fromPosition, targetPosition), abs(fromPosition - targetPosition) +1)
        innerDragListener?.onItemDrag(fromPosition, targetPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.itemList.removeAt(viewHolder.bindingAdapterPosition)
        innerSwipeListener?.onItemSwipe(viewHolder.bindingAdapterPosition, direction)
    }

    fun <L : Listener<VH>> setListener(listener: L) {
        when (listener) {
            is ItemDragListener<*> -> innerDragListener = listener as ItemDragListener<VH>
            is ItemSwipeListener<*> -> innerSwipeListener = listener as ItemSwipeListener<VH>
        }
    }
}