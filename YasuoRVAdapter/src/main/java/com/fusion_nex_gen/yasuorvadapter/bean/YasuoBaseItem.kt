package com.fusion_nex_gen.yasuorvadapter.bean

/**
 * Data base class, optional extends
 */
interface YasuoBaseItem {
    //该item在staggeredGrid中是否占满
    //Is this type of layout full in the staggeedgrid
    var staggeredGridFullSpan: Boolean
    //该类型布局在grid中的占比
    //The proportion of this type of layout in Grid
    var gridSpan: Int
    //该类型布局是否吸顶
    //Is this type of layout a sticky header
    var sticky: Boolean
}