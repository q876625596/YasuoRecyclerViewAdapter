package com.fusion_nex_gen.yasuorvadapter.bindingAdapter

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

/**
 * 设置背景色
 * Set background color
 */
@BindingAdapter(value = ["android:background"])
fun View.setYasuoBackground(background: Any?) {
    if (background is Drawable) {
        setBackground(background)
        return
    }
    if (background is Int) {
        setBackgroundColor(background)
    }

}

/**
 * 设置文本尺寸
 * Set text size
 */
@BindingAdapter("android:textSize")
fun setYasuoTextSizeSp(view: TextView, size: Float) {
    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
}
