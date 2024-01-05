package com.segx01.testfirenase.utils.view

import android.view.View

fun View.showWithAnimation(duration: Long = 300L) {
    visibility = View.VISIBLE
    animate().alpha(1f).setDuration(duration)
}

fun View.hideWithAnimation(duration: Long = 300L) {
    visibility = View.GONE
    animate().alpha(0f).setDuration(duration)
}
