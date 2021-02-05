package com.fusion_nex_gen.yasuorvadapter.decoration

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class DrawableBean {
    var leftDrawable: Drawable? = null
    var topDrawable: Drawable? = null
    var rightDrawable: Drawable? = null
    var bottomDrawable: Drawable? = null

    constructor(
        leftDrawable: Drawable? = null,
                topDrawable: Drawable? = null,
                rightDrawable: Drawable? = null,
                bottomDrawable: Drawable? = null) {
        leftDrawable?.let {
            this.leftDrawable = it
        }
        topDrawable?.let {
            this.topDrawable = it
        }
        rightDrawable?.let {
            this.rightDrawable = it
        }
        bottomDrawable?.let {
            this.bottomDrawable = it
        }
    }

    constructor(context: Context,
                leftDrawableRes: Int = 0,
                topDrawableRes: Int = 0,
                rightDrawableRes: Int = 0,
                bottomDrawableRes: Int = 0) {
        try {
            ContextCompat.getDrawable(context, leftDrawableRes)?.let {
                leftDrawable = it
            }
        } catch (e: Resources.NotFoundException) {
        }
        try {
            ContextCompat.getDrawable(context, topDrawableRes)?.let {
                topDrawable = it
            }
        } catch (e: Resources.NotFoundException) {

        }
        try {
            ContextCompat.getDrawable(context, rightDrawableRes)?.let {
                rightDrawable = it
            }
        } catch (e: Resources.NotFoundException) {

        }
        try {
            ContextCompat.getDrawable(context, bottomDrawableRes)?.let {
                bottomDrawable = it
            }
        } catch (e: Resources.NotFoundException) {

        }
    }

}