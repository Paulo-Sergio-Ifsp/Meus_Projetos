package com.paulo.controle_gastos.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {

    fun smsPermissions(): Array<String> = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    fun hasSmsPermission(context: Context): Boolean =
        smsPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun postNotificationPermission(): String? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    fun hasPostNotificationsPermission(context: Context): Boolean {
        val permission = postNotificationPermission() ?: return true
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun captureFullyEnabled(context: Context): Boolean =
        hasSmsPermission(context) &&
            hasPostNotificationsPermission(context) &&
            NotificationUtils.isNotificationListenerEnabled(context)
}
