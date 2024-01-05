package com.segx01.testfirenase.utils.activity

import android.app.Activity
import android.view.View
import androidx.core.view.WindowCompat
import com.google.android.material.snackbar.Snackbar
import com.segx01.testfirenase.R

fun Activity.updateStatusBarIcons(darkStatusBar: Boolean) {
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
    insetsController.let {
        it.isAppearanceLightStatusBars = darkStatusBar
        it.isAppearanceLightNavigationBars = darkStatusBar
    }
}

fun showMessage(message: String, view: View) {
    Snackbar.make(view, message, Snackbar.LENGTH_SHORT).setAnchorView(R.id.fab).show()
}