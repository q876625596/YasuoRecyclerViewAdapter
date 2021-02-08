package com.fusion_nex_gen.yasuorvadapter.bean


open class YasuoFoldItem(
    //下一级列表
    var list: YasuoList<YasuoFoldItem> = YasuoList(),
    //是否已展开
    var isExpand: Boolean = false,
    //父级hash，展开后才会赋值
    var parentHash: Int? = null,
    override var staggeredGridFullSpan: Boolean = false,
    override var gridSpan: Int = 0,
    override var sticky: Boolean = false
) : YasuoBaseItem
