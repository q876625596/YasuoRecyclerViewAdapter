package com.fusion_nex_gen.yasuorecyclerviewadapter.model

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.fusion_nex_gen.yasuorvadapter.bean.YasuoFoldItem

data class ImageBean(
    val image: MutableLiveData<Drawable>,
    val canClick: MutableLiveData<Boolean> = MutableLiveData(false),
) : YasuoFoldItem(autoExpand = true)
