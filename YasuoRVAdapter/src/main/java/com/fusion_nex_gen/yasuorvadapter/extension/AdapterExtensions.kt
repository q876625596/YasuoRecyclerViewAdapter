package com.fusion_nex_gen.yasuorvadapter.extension

import com.fusion_nex_gen.yasuorvadapter.YasuoBaseRVAdapter
import com.fusion_nex_gen.yasuorvadapter.YasuoRVDataBindingAdapter

/***************************************************************************
 *                                                                         *
 *                   AbsRecyclerViewAdapter属性拓展                         *
 *                                                                         *
 ***************************************************************************/

/**
 * 获取所有数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.getItems(): MutableList<T> = itemList

/**
 * 获取指定位置的数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.getItem(index: Int): T = itemList[index]

/**
 * 添加一项数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.addItem(item: T) {
    this.itemList.add(item)
    this.notifyItemInserted(this.itemList.size - 1)
}

/**
 * 在指定位置添加数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.addItem(index: Int, item: T) {
    checkDataValid(index)
    this.itemList.add(index, item)
    this.notifyItemInserted(index)
}

/**
 * 添加数据集合
 */
fun <T : Any, Adapter : YasuoBaseRVAdapter<T, *>> Adapter.addItems(items: Collection<T>): Adapter {
    val previousIndex = this.itemList.size - 1
    this.itemList.addAll(items)
    this.notifyItemRangeInserted(previousIndex + 1, items.size)
    return this
}

/**
 * 在指定位置添加数据集合
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.addItems(index: Int, items: Collection<T>) {
    checkDataValid(index)
    this.itemList.addAll(index, items)
    this.notifyItemRangeInserted(index, items.size)
}

/**
 * 添加数据集合
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.putItems(items: Collection<T>) {
    val previousIndex = this.itemList.size - 1
    this.itemList.clear()
    this.itemList.addAll(items)
    this.notifyItemRangeInserted(previousIndex + 1, items.size)
}

/**
 * 添加数据集合
 */
fun <T : Any> YasuoRVDataBindingAdapter.putItems(items: Collection<T>) {
    this.itemList.clear()
    this.itemList.addAll(items)
}

/**
 * 移除指定位置的数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.removeItemAt(index: Int) {
    checkDataValid(index)
    this.itemList.removeAt(index)
    this.notifyItemRemoved(index)
}

/**
 * 移除指定位置及之后的所有数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.removeItemFrom(start: Int) {
    checkDataValid(start)
    val removedItems = this.itemList.filterIndexed { index, _ -> index >= start }
    this.itemList.removeAll(removedItems)
    this.notifyItemRangeRemoved(start, removedItems.size)
}

/**
 * 移除指定范围的数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.removeItemRange(start: Int, end: Int) {
    checkDataValid(start)
    checkDataValid(end)
    val removedItems = this.itemList.filterIndexed { index, _ -> index in start..end }
    this.itemList.removeAll(removedItems)
    this.notifyItemRangeRemoved(start, removedItems.size)
}

/**
 * 移除指定范围的数据
 */
fun <T : Any> YasuoBaseRVAdapter<T, *>.clear() {
    this.itemList.clear()
    this.notifyDataSetChanged()
}

private fun <T : Any> YasuoBaseRVAdapter<T, *>.checkDataValid(index: Int) {
    if (index < 0 || index >= this.itemList.size) {
        throw IndexOutOfBoundsException("Index must be large than zero and less than items' size")
    }
}