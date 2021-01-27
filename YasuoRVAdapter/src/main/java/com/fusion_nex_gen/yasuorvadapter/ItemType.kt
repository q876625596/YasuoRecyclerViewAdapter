package com.fusion_nex_gen.yasuorvadapter

import android.view.View
import androidx.viewbinding.ViewBinding

//实体类型type
data class ItemType(
    //item的布局id
    val itemLayoutId: Int,
    //item的绑定id
    val variableId: Int? = null,
    val createBindingFun:((view: View)->ViewBinding)? = null
)