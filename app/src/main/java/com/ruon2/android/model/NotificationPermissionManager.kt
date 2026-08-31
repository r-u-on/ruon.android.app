package com.ruon2.android.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.ruon2.android.util.UserLog
import com.ruon2.app.R

class NotificationPermissionManager(activityResultRegistry: ActivityResultRegistry) {

    companion object {

        private const val TAG = "NotificationPermissionManager"
        private const val LAUNCHER_KEY = "NotificationPermissionManagerLauncherKey"
    }

    private val notificationPermissionLauncher = activityResultRegistry.register(
        LAUNCHER_KEY,
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val message = if (isGranted) {
            "notifications permission granted"
        } else {
            "notifications permission not granted"
        }

        UserLog.i(
            TAG,
            message
        )
    }

    fun handleNotificationPermission(activity: ComponentActivity) {
        // cases where permission is not needed
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val shouldShowRequestPermissionRationale =
            activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)

        when {
            isNotificationPermissionGranted(activity) -> {
                MyPreferenceManager.setShouldShowNotificationPermissionRational(
                    activity,
                    false
                )
            }

            shouldShowRequestPermissionRationale -> {
                // after notification permission request was denied once
                launchNotificationPermissionRequest(notificationPermissionLauncher)
                MyPreferenceManager.setShouldShowNotificationPermissionRational(
                    activity,
                    true
                )
            }

            MyPreferenceManager.shouldShowNotificationPermissionRational(activity) -> {
                // showing rational after notification permission request was denied twice
                showNotificationPermissionRational(activity)
            }

            else -> {
                // first time requesting permission
                launchNotificationPermissionRequest(notificationPermissionLauncher)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun launchNotificationPermissionRequest(
        notificationPermissionLauncher: ActivityResultLauncher<String>,
    ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun showNotificationPermissionRational(context: Context) {
        val positiveButtonText =
            context.getString(R.string.notification_permission_required_dialog_go_to_settings)
        val negativeButtonText = context.getString(R.string.notification_permission_required_cancel)
        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            val uri = Uri.fromParts(
                "package",
                context.packageName,
                null
            )
            data = uri
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.notification_permission_required_dialog_title))
            .setMessage(context.getString(R.string.notification_permission_required_dialog_message))
            .setPositiveButton(positiveButtonText) { _, _ ->
                // Navigate to app settings
                context.startActivity(appSettingsIntent)
            }
            .setNegativeButton(negativeButtonText) { dialog, _ -> dialog.dismiss() }
            .create()

        // Set the color for the cancel button to red
        dialog.setOnShowListener {
            val color = ContextCompat.getColor(
                context,
                android.R.color.holo_red_dark
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(color)
        }

        dialog.show()
    }
}