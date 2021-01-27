package com.fusion_nex_gen.yasuorvadapter

import androidx.databinding.ObservableArrayList

open class ObList<T> : ObservableArrayList<T>() {

    fun removeAtIndexes(vararg indexes: Int) {
        indexes.forEach {
            this.removeAt(it)
        }
    }

    fun removeFrom(start: Int) {
        /*val removedItems = this.filterIndexed { index, _ -> index >= start }
        this.removeAll(removedItems)*/
        if (start >= size) {
            return
        }
        this.removeRange(start, this.size)
    }

}