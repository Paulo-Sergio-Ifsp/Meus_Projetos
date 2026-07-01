package com.paulo.controle_gastos.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.paulo.controle_gastos.R
import com.paulo.controle_gastos.util.NotificationUtils
import com.paulo.controle_gastos.util.PermissionHelper

@Composable
fun CapturePermissionsCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var smsGranted by remember { mutableStateOf(PermissionHelper.hasSmsPermission(context)) }
    var notificationsGranted by remember {
        mutableStateOf(PermissionHelper.hasPostNotificationsPermission(context))
    }
    var listenerEnabled by remember {
        mutableStateOf(NotificationUtils.isNotificationListenerEnabled(context))
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                smsGranted = PermissionHelper.hasSmsPermission(context)
                notificationsGranted = PermissionHelper.hasPostNotificationsPermission(context)
                listenerEnabled = NotificationUtils.isNotificationListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val smsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { smsGranted = PermissionHelper.hasSmsPermission(context) }

    val postNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notificationsGranted = PermissionHelper.hasPostNotificationsPermission(context) }

    if (smsGranted && notificationsGranted && listenerEnabled) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.capture_permissions_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.capture_permissions_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            PermissionRow(
                label = stringResource(R.string.permission_sms),
                granted = smsGranted,
                onAction = {
                    smsLauncher.launch(PermissionHelper.smsPermissions())
                },
                actionLabel = stringResource(R.string.permission_grant)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionRow(
                    label = stringResource(R.string.permission_post_notifications),
                    granted = notificationsGranted,
                    onAction = {
                        postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    actionLabel = stringResource(R.string.permission_grant)
                )
            }

            PermissionRow(
                label = stringResource(R.string.permission_notification_listener),
                granted = listenerEnabled,
                onAction = { NotificationUtils.openNotificationListenerSettings(context) },
                actionLabel = stringResource(R.string.permission_open_settings),
                useOutlinedButton = true
            )
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    granted: Boolean,
    onAction: () -> Unit,
    actionLabel: String,
    useOutlinedButton: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }

        if (!granted) {
            if (useOutlinedButton) {
                OutlinedButton(onClick = onAction) {
                    Text(actionLabel)
                }
            } else {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}
