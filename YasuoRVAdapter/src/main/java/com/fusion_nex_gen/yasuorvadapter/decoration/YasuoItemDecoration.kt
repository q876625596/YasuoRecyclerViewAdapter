package com.fusion_nex_gen.yasuorvadapter.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.fusion_nex_gen.yasuorvadapter.R

fun <RV : RecyclerView> RV.addYasuoDecoration(block: YasuoItemDecoration.() -> YasuoItemDecoration) {
    this.addItemDecoration(YasuoItemDecoration().block())
}

class YasuoItemDecoration : RecyclerView.ItemDecoration() {
    val decorations: SparseArray<DrawableBean> = SparseArray()
    private var firstDecoration: DrawableBean? = null
    private var lastDecoration: DrawableBean? = null

    /**
     * 系统默认分割线
     * System default split line
     */
    val defaultRes = R.drawable.yasuo_default_divider

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val lp = parent.layoutManager
        val childType = lp!!.getItemViewType(view)
        val drawable = when {
            isFirstPosition(view, parent) -> firstDecoration ?: decorations.get(childType)
            isLastPosition(view, parent) -> lastDecoration ?: decorations.get(childType)
            else -> decorations.get(childType)
        } ?: return

        //为decoration预留位置
        //Reserve space for decoration
        drawable.apply {
            outRect.left = rightDrawable?.intrinsicWidth ?: 0
            outRect.right = leftDrawable?.intrinsicWidth ?: 0
            outRect.top = topDrawable?.intrinsicWidth ?: 0
            outRect.bottom = bottomDrawable?.intrinsicWidth ?: 0
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {// >=0 && <=childCount-1
            val child = parent.getChildAt(i)
            val childViewType = parent.layoutManager!!.getItemViewType(child)
            val drawableBean = when {
                isFirstPosition(child, parent) -> firstDecoration ?: decorations.get(childViewType)
                isLastPosition(child, parent) -> lastDecoration ?: decorations.get(childViewType)
                else -> decorations.get(childViewType)
            } ?: return
            drawableBean.topDrawable?.apply {
                setBounds(child.left - intrinsicWidth, child.top - intrinsicHeight, child.right + intrinsicWidth, child.top)
                draw(c)
            }
            drawableBean.bottomDrawable?.apply {
                setBounds(child.left - intrinsicWidth, child.bottom, child.right + intrinsicWidth, child.bottom + intrinsicHeight)
                draw(c)
            }
            drawableBean.leftDrawable?.apply {
                setBounds(child.left - intrinsicWidth, child.top, child.left, child.top + child.height)
                draw(c)
            }
            drawableBean.rightDrawable?.apply {
                setBounds(child.left + child.width, child.top, child.left + child.width + intrinsicWidth, child.top + child.height)
                draw(c)
            }
        }
    }

    private fun isFirstPosition(view: View, parent: RecyclerView): Boolean {
        return parent.getChildAdapterPosition(view) == 0
    }

    private fun isLastPosition(view: View, parent: RecyclerView): Boolean {
        return parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1
    }

    /*设置decoration*/

    fun setDecoration(
        type: Int,
        drawableBean: DrawableBean
    ): YasuoItemDecoration {
        decorations.put(type, drawableBean)
        return this
    }

    fun setDecoration(
        type: Int,
        allDrawable: Drawable
    ): YasuoItemDecoration {
        decorations.put(type, DrawableBean(allDrawable, allDrawable, allDrawable, allDrawable))
        return this
    }

    fun setDecoration(
        type: Int,
        context: Context,
        allDrawableRes: Int
    ): YasuoItemDecoration {
        decorations.put(type, DrawableBean(context, allDrawableRes, allDrawableRes, allDrawableRes, allDrawableRes))
        return this
    }

    fun setDecoration(
        type: Int,
        leftDrawable: Drawable? = null,
        topDrawable: Drawable? = null,
        rightDrawable: Drawable? = null,
        bottomDrawable: Drawable? = null,
    ) {
        decorations.put(type, DrawableBean(leftDrawable, topDrawable, rightDrawable, bottomDrawable))
    }

    fun setDecoration(
        type: Int,
        context: Context,
        leftDrawableRes: Int = 0,
        topDrawableRes: Int = 0,
        rightDrawableRes: Int = 0,
        bottomDrawableRes: Int = 0
    ) {
        decorations.put(type, DrawableBean(context, leftDrawableRes, topDrawableRes, rightDrawableRes, bottomDrawableRes))
    }

    fun setFirstDecoration(drawableBean: DrawableBean): YasuoItemDecoration {
        firstDecoration = drawableBean
        return this
    }

    fun setFirstDecoration(
        allDrawable: Drawable
    ): YasuoItemDecoration {
        firstDecoration = DrawableBean(allDrawable, allDrawable, allDrawable, allDrawable)
        return this
    }

    fun setFirstDecoration(
        context: Context,
        allDrawableRes: Int
    ): YasuoItemDecoration {
        firstDecoration = DrawableBean(context, allDrawableRes, allDrawableRes, allDrawableRes, allDrawableRes)
        return this
    }

    fun setFirstDecoration(
        leftDrawable: Drawable? = null,
        topDrawable: Drawable? = null,
        rightDrawable: Drawable? = null,
        bottomDrawable: Drawable? = null,
    ): YasuoItemDecoration {
        firstDecoration = DrawableBean(leftDrawable, topDrawable, rightDrawable, bottomDrawable)
        return this
    }

    fun setFirstDecoration(
        context: Context,
        leftDrawableRes: Int = 0,
        topDrawableRes: Int = 0,
        rightDrawableRes: Int = 0,
        bottomDrawableRes: Int = 0
    ): YasuoItemDecoration {
        firstDecoration = DrawableBean(context, leftDrawableRes, topDrawableRes, rightDrawableRes, bottomDrawableRes)
        return this
    }

    fun setLastDecoration(drawableBean: DrawableBean): YasuoItemDecoration {
        lastDecoration = drawableBean
        return this
    }

    fun setLastDecoration(
        allDrawable: Drawable
    ): YasuoItemDecoration {
        lastDecoration = DrawableBean(allDrawable, allDrawable, allDrawable, allDrawable)
        return this
    }

    fun setLastDecoration(
        context: Context,
        allDrawableRes: Int
    ): YasuoItemDecoration {
        lastDecoration = DrawableBean(context, allDrawableRes, allDrawableRes, allDrawableRes, allDrawableRes)
        return this
    }

    fun setLastDecoration(
        leftDrawable: Drawable? = null,
        topDrawable: Drawable? = null,
        rightDrawable: Drawable? = null,
        bottomDrawable: Drawable? = null,
    ): YasuoItemDecoration {
        lastDecoration = DrawableBean(leftDrawable, topDrawable, rightDrawable, bottomDrawable)
        return this
    }

    fun setLastDecoration(
        context: Context,
        leftDrawableRes: Int = 0,
        topDrawableRes: Int = 0,
        rightDrawableRes: Int = 0,
        bottomDrawableRes: Int = 0
    ): YasuoItemDecoration {
        lastDecoration = DrawableBean(context, leftDrawableRes, topDrawableRes, rightDrawableRes, bottomDrawableRes)
        return this
    }
}