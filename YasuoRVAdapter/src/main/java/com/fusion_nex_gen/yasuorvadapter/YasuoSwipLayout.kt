package com.fusion_nex_gen.yasuorvadapter

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup

class YasuoSwipLayout : ViewGroup {
    constructor(context: Context?) : super(context, null)
    constructor(context: Context?, attrs: AttributeSet) : super(context, attrs, 0)
//    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int):super(context, attrs, defStyleAttr, 0)
//    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int):super(context, attrs, defStyleAttr, defStyleRes)

    var isLeftSwipe = true
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childCount = childCount
        var left = 0 + paddingLeft
        var right = 0 + paddingLeft
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.visibility != GONE) {
                if (i == 0) { //第一个子View
                    childView.layout(left, paddingTop, left + childView.measuredWidth, paddingTop + childView.measuredHeight)
                    left += childView.measuredWidth
                } else {
                    if (isLeftSwipe) {
                        childView.layout(left, paddingTop, left + childView.measuredWidth, paddingTop + childView.measuredHeight)
                        left += childView.measuredWidth
                    } else {
                        childView.layout(right - childView.measuredWidth, paddingTop, right, paddingTop + childView.measuredHeight)
                        right -= childView.measuredWidth
                    }
                }
            }
        }
    }
}