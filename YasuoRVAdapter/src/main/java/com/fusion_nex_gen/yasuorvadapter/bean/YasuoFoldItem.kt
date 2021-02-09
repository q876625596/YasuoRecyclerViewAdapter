package com.fusion_nex_gen.yasuorvadapter.bean

/**
 * Fold layout base class
 */
open class YasuoFoldItem(
    //下一级列表
    //Next level list
    var list: YasuoList<YasuoFoldItem> = YasuoList(),
    //是否已展开
    //Expanded
    var isExpand: Boolean = false,
    //父级hash，展开后才会赋值
    //Parent hash, which will be assigned after expansion
    var parentHash: Int? = null,
    override var staggeredGridFullSpan: Boolean = false,
    override var gridSpan: Int = 0,
    override var sticky: Boolean = false
) : YasuoBaseItem
