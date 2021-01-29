package com.fusion_nex_gen.yasuorvadapter.bean

import android.view.View
import androidx.viewbinding.ViewBinding

//实体类型type
data class YasuoItemType(
    //item的布局id
    val itemLayoutId: Int,
    //item的绑定id
    val variableId: Int? = null,
    //创建ViewBinding
    val createBindingFun: ((view: View) -> ViewBinding)? = null,
    override var sticky: Boolean = false
) : StickyItem