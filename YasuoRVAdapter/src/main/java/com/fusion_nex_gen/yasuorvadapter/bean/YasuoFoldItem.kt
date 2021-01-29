package com.fusion_nex_gen.yasuorvadapter.bean


open class YasuoFoldItem(
    var list: YasuoList<YasuoFoldItem> = YasuoList(),
    var isExpand: Boolean = false,
    var autoExpand: Boolean = false,
    var span: Int = 1,
    var parentHash: Int? = null,
    override var sticky: Boolean = false
) : StickyItem
