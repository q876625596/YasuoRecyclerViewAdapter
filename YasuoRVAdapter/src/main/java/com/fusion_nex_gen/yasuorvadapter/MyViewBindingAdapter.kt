package com.fusion_nex_gen.yasuorvadapter

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter

/**
 * 设置背景色
 */
@BindingAdapter(value = ["android:background"])
fun View.setMyBackground(background: Any?) {
    if (background is Drawable) {
        setBackground(background)
        return
    }
    if (background is Int) {
        setBackgroundColor(background)
    }

}


@BindingAdapter("android:textSize")
fun setTextSizeSp(view: TextView, size: Float) {
    view.setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
}
