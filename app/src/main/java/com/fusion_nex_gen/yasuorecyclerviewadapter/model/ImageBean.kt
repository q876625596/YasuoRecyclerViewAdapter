package com.fusion_nex_gen.yasuorecyclerviewadapter.model

import android.graphics.drawable.Drawable
import androidx.lifecycle.MutableLiveData
import com.fusion_nex_gen.yasuorvadapter.FoldItem

data class ImageBean(
    val image: MutableLiveData<Drawable>,
) : FoldItem(autoExpand = true)
