package com.fusion_nex_gen.yasuorvadapter

import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener

/**
 * Item 滑动时触发
 */
interface ItemSwipeListener<in VH> : Listener<VH> {
    /**
     * @param position
     * @param direction : 方向{ @link ItemTouchHelper.Left...}
     */
    fun onItemSwipe(position: Int, direction: Int)
}