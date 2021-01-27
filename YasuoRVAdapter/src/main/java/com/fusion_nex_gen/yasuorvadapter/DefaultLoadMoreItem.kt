package com.fusion_nex_gen.yasuorvadapter

import android.graphics.Color
import androidx.lifecycle.MutableLiveData

data class DefaultLoadMoreItem(
    /**
     * loadMore文本
     */
    val text: MutableLiveData<String> = MutableLiveData("LoadMore..."),
    /**
     * loadMoreLayout背景，详见[com.fusion_nex_gen.yasuorvadapter.setMyBackground]
     */
    val bgColor: MutableLiveData<Any> = MutableLiveData(Color.WHITE),
    /**
     * loadMore字体颜色
     */
    val textColor: MutableLiveData<Int> = MutableLiveData(Color.GRAY),
    /**
     * 文本尺寸，sp，详见[com.fusion_nex_gen.yasuorvadapter.setTextSizeSp]
     */
    val textSize: MutableLiveData<Int> = MutableLiveData(14),
)