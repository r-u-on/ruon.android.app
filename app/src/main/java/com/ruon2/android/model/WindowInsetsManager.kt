package com.ruon2.android.model

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.ruon2.android.util.UserLog

class WindowInsetsManager {

    companion object {

        private const val TAG = "WindowInsetsManager"
    }

    fun handleWindowInsets(
        activity: AppCompatActivity,
        container: View
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        with(activity) {
            applyDarkStatusBarIcons()
            setOnApplyWindowInsetsListener()
            manageTopContainerPadding(
                window,
                container
            )
        }
    }

    private fun Activity.applyDarkStatusBarIcons() {
        val windowInsetsController = WindowCompat.getInsetsController(
            window,
            window.decorView
        )
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    /**
     * Sets an OnApplyWindowInsetsListener to adjust padding for edge-to-edge mode,
     * which is default on Android 15 (API level 35), handling status bar, navigation bar, and IME.
     */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun AppCompatActivity.setOnApplyWindowInsetsListener() {
        window.decorView.setOnApplyWindowInsetsListener { containerView, insets ->
            UserLog.e(
                TAG,
                "InsetsListener call"
            )

            val systemBarsInsets = insets.getInsets(WindowInsets.Type.systemBars())
            val statusBarHeight = systemBarsInsets.top

            // No top padding needed if ActionBar is present.
            val topPadding = if (supportActionBar != null) 0 else statusBarHeight

            // Set the padding for the container view, accounting for the status bar, ActionBar, and navigation bar
            containerView.setPadding(
                systemBarsInsets.left,
                topPadding,
                systemBarsInsets.right,
                systemBarsInsets.bottom
            )

            insets
        }
    }

    /** Adds top padding to the container to prevent the ActionBar from overlapping the content. */
    private fun AppCompatActivity.manageTopContainerPadding(
        window: Window,
        container: View
    ) {
        window.decorView.post {
            supportActionBar?.let { actionBar ->
                container.setPadding(
                    container.paddingLeft,
                    actionBar.height,
                    container.paddingRight,
                    container.paddingBottom
                )
            }
        }
    }
}