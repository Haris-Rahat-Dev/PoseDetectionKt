package com.example.posedetectionkt.utils

import android.app.Activity
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

object WindowManager {

    fun statusBarManager(activity: Activity, color: Int, isDarkIconsColor: Boolean) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(activity, color)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isDarkIconsColor
        }
    }
}