package com.fusion_nex_gen.yasuorecyclerviewadapter.model

import androidx.lifecycle.MutableLiveData
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoNormalItem

data class HeaderTwoBean(
    val headerOneBgColor: MutableLiveData<Int>
) : YasuoNormalItem()
