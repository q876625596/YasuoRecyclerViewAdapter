package com.fusion_nex_gen.yasuorvadapter.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.util.SparseArray
import android.view.View

class DecorationForVerticalList private constructor(
    val decorations: SparseArray<DrawableBean>,
    val showFirst: Boolean,
    val showLast: Boolean,
    val decorationMode: DecorationMode,
    val isReverse: Boolean,
    val firstDecoration: DrawableBean?,
    val lastDecoration: DrawableBean?
) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    private constructor(builder: Builder) : this(
            builder.decorations,
            builder.showFirst,
            builder.showLast,
            builder.decorationMode,
            builder.isReverse,
            builder.firstDecoration,
            builder.lastDecoration)

    companion object {
        fun build(init: Builder.() -> Unit) = Builder(init).build()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val childType = parent.layoutManager!!.getItemViewType(view)
        val drawable = if (!isFirstPosition(view, parent) && !isLastPosition(view, parent)) {
            decorations.get(childType)
        } else {
            if (isFirstPosition(view, parent)) {
                firstDecoration ?: decorations.get(childType)
            } else {
                lastDecoration ?: decorations.get(childType)
            }
        }
        //为decoration预留位置
        drawable?.apply {
            leftDrawable?.let {
                outRect.left = it.intrinsicWidth
            }
            topDrawable?.let {
                //如果反向
                if (isReverse) {
                    //如果是反向列表的最后一个，那么判断顶部decoration是否显示
                    if (isLastPosition(view, parent)) {
                        if (showLast) {
                            outRect.top = it.intrinsicHeight
                        }
                        return@let
                    }
                    outRect.top = it.intrinsicHeight
                    return@let
                }
                //如果是第一个item
                if (isFirstPosition(view, parent)) {
                    //并且需要显示顶部的decoration时，才预留位置
                    if (showFirst && !isReverse) {
                        outRect.top = it.intrinsicHeight
                    }
                } else {
                    outRect.top = it.intrinsicHeight
                }
            }
            rightDrawable?.let {
                outRect.right = it.intrinsicWidth
            }
            bottomDrawable?.let {
                //如果反向
                if (isReverse) {
                    //并且是反向列表中的第一个，那么判断底部decoration是否显示
                    if (isFirstPosition(view, parent)) {
                        if (showFirst) {
                            outRect.bottom = it.intrinsicHeight
                        }
                        return@let
                    }
                    outRect.bottom = it.intrinsicHeight
                    return@let
                }
                //如果是最后一个item
                if (isLastPosition(view, parent)) {
                    //并且需要显示底部的decoration时，才预留位置
                    if (showLast && !isReverse) {
                        outRect.bottom = it.intrinsicHeight
                    }
                } else {
                    outRect.bottom = it.intrinsicHeight
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {// >=0 && <=childCount-1
            val child = parent.getChildAt(i)
            val childViewType = parent.layoutManager!!.getItemViewType(child)
            val drawableBean: DrawableBean?
            drawableBean = if (i != 0 && i != childCount - 1) {
                decorations.get(childViewType)
            } else {
                if (i == 0) {
                    firstDecoration ?: decorations.get(childViewType)
                } else {
                    lastDecoration ?: decorations.get(childViewType)
                }
            }
            drawableBean?.leftDrawable?.let {
                it.setBounds(
                        when (decorationMode) {
                            //如果是占满或者贴在rv左边的内边框，那么左边border的left位置在rv的左侧内边框开始位置
                            DecorationMode.MODE_FILL, DecorationMode.MODE_PARENT -> left
                            //否则就在itemView的左边-decoration的宽度的位置
                            else -> child.left - it.intrinsicWidth
                        }, child.top,
                        when (decorationMode) {
                            //如果是占满或者贴在itemView上，那么左边border的right位置在itemView的左边
                            DecorationMode.MODE_FILL, DecorationMode.MODE_CHILD -> child.left
                            //否则就在rv左侧内边框+decoration的宽度的位置
                            else -> left + it.intrinsicWidth
                        }, child.bottom)
                it.draw(c)
            }
            drawableBean?.rightDrawable?.let {
                it.setBounds(
                        when (decorationMode) {
                            //如果是占满或者贴在itemView上，那么右边border的left位置在itemView的右边
                            DecorationMode.MODE_FILL, DecorationMode.MODE_CHILD -> child.right
                            //否则就在rv右边内边框-decoration的宽度的位置
                            else -> right - it.intrinsicWidth
                        }, child.top,
                        when (decorationMode) {
                            //如果是占满或者贴在rv右边的内边框，那么右边border的right位置在rv的右边内边框开始位置
                            DecorationMode.MODE_FILL, DecorationMode.MODE_PARENT -> right
                            //否则就在itemView的右边+decoration的高度的位置
                            else -> child.right + it.intrinsicWidth
                        }, child.bottom)
                it.draw(c)
            }
            drawableBean?.topDrawable?.let {
                it.setBounds(left, child.top - it.intrinsicHeight, right, child.top)
                it.draw(c)
            }
            drawableBean?.bottomDrawable?.let {
                it.setBounds(left, child.bottom, right, child.bottom + it.intrinsicHeight)
                it.draw(c)
            }
        }
    }

    private fun isFirstPosition(view: View, parent: androidx.recyclerview.widget.RecyclerView): Boolean {
        return parent.getChildAdapterPosition(view) == 0
    }

    private fun isLastPosition(view: View, parent: androidx.recyclerview.widget.RecyclerView): Boolean {
        return parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1
    }


    /**
     * Builder 内部类
     */
    class Builder private constructor() {

        constructor(init: Builder.() -> Unit) : this() {
            init()
        }

        fun build(): DecorationForVerticalList {
            return DecorationForVerticalList(decorations,
                    showFirst,
                    showLast,
                    decorationMode,
                    isReverse,
                    firstDecoration,
                    lastDecoration)
        }

        var decorations = SparseArray<DrawableBean>()
        //是否显示第一个item 的顶部decoration
        var showFirst = true
        //是否显示最后一个item 的底部decoration
        var showLast = true
        //当item的宽或高小于RecyclerView时，decoration的显示方式
        var decorationMode = DecorationMode.MODE_PARENT
        //是否反向
        var isReverse = false
        //第一个item单独的decoration
        var firstDecoration: DrawableBean? = null
        //最后一个item单独的decoration
        var lastDecoration: DrawableBean? = null

        fun decorations(type: Int, init: Builder.() -> DrawableBean) = apply {
            decorations.put(type, init())
        }

        fun showFirst(init: Builder.() -> Boolean) = apply {
            showFirst = init()
        }

        fun showLast(init: Builder.() -> Boolean) = apply {
            showLast = init()
        }

        fun decorationMode(init: Builder.() -> DecorationMode) = apply {
            decorationMode = init()
        }

        fun isReverse(init: Builder.() -> Boolean) = apply {
            isReverse = init()
        }

        fun firstDecoration(init: Builder.() -> DrawableBean) = apply {
            firstDecoration = init()
        }

        fun lastDecoration(init: Builder.() -> DrawableBean) = apply {
            lastDecoration = init()
        }
    }

}