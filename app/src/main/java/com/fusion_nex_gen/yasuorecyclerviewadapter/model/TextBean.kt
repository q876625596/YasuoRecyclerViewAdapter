package com.fusion_nex_gen.yasuorecyclerviewadapter.model

import androidx.lifecycle.MutableLiveData
import com.fusion_nex_gen.yasuorvadapter.FoldItem

data class TextBean(
    val text: MutableLiveData<String>
):FoldItem()
