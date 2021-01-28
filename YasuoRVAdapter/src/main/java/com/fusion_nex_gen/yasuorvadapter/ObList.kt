package com.fusion_nex_gen.yasuorvadapter

import androidx.databinding.ObservableArrayList
import kotlin.math.min

open class ObList<T> : ObservableArrayList<T>() {

    fun removeAtIndexes(vararg indexes: Int) {
        indexes.forEach {
            this.removeAt(it)
        }
    }

    fun removeFrom(start: Int, to: Int = size) {
        /*val removedItems = this.filterIndexed { index, _ -> index >= start }
        this.removeAll(removedItems)*/
        if (start >= size) {
            return
        }
        val end = min(to, size)
        this.removeRange(start, end)
    }

}