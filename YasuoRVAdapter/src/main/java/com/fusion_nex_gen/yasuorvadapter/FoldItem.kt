package com.fusion_nex_gen.yasuorvadapter


open class FoldItem(
    var list: ObList<FoldItem> = ObList(),
    var isExpand: Boolean = false,
    var autoExpand: Boolean = false,
    var fullSpan: Boolean = false,
    var parentHash: Int? = null
)
