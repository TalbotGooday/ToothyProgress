package com.goodayapps.toothyprogress

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

internal fun Context.convertDpToPixel(dp: Int): Int {
	val metrics = resources.displayMetrics
	return dp * (metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}
