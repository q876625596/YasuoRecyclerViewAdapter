package com.fusion_nex_gen.yasuorvadapter.bean


open class YasuoFoldItem(
    var list: YasuoList<YasuoFoldItem> = YasuoList(),
    var isExpand: Boolean = false,
    var autoExpand: Boolean = false,
    var parentHash: Int? = null,
    override var staggeredGridFullSpan: Boolean = false,
    override var gridSpan: Int = 0,
    override var sticky: Boolean = false
) : YasuoBaseItem
