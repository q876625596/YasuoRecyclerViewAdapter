package com.fusion_nex_gen.yasuorvadapter

import com.fusion_nex_gen.yasuorvadapter.interfaces.Listener

/**
 * Item 被拖拽时触发
 */
interface ItemDragListener<in VH> : Listener<VH> {
    /**
     * @param from 开始位置
     * @param target 结束位置
     */
    fun onItemDrag(from: Int, target: Int)
}