package com.fusion_nex_gen.yasuorvadapter.bean

import android.graphics.Color
import androidx.lifecycle.MutableLiveData

data class DefaultLoadMoreItem(
    /**
     * loadMore text
     */
    val text: MutableLiveData<String> = MutableLiveData("LoadMore..."),
    /**
     * loadMoreLayout background，[com.fusion_nex_gen.yasuorvadapter.bindingAdapter.setYasuoBackground]
     */
    val bgColor: MutableLiveData<Any> = MutableLiveData(Color.WHITE),
    /**
     * loadMore font color
     */
    val textColor: MutableLiveData<Int> = MutableLiveData(Color.GRAY),
    /**
     * size，sp，[com.fusion_nex_gen.yasuorvadapter.bindingAdapter.setYasuoTextSizeSp]
     */
    val textSize: MutableLiveData<Int> = MutableLiveData(14),
)