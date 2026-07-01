package com.paulo.controle_gastos.util

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.paulo.controle_gastos.NotificationReaderService

object NotificationUtils {

    private val BANK_PACKAGE_HINTS = listOf(
        "nubank", "itau", "bradesco", "santander", "c6bank", "bancointer",
        "com.bb", "caixa", "picpay", "mercadopago", "neon", "next", "original",
        "willbank", "pagbank", "pagseguro"
    )

    fun extractText(extras: Bundle): Pair<String, String> {
        val titulo = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getString("android.title")
            ?: ""

        val texto = buildString {
            appendPart(extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
            appendPart(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString())
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.forEach { line ->
                appendPart(line?.toString())
            }
            if (isEmpty()) {
                appendPart(extras.getString("android.text"))
            }
        }

        return titulo to texto
    }

    fun isBankRelated(packageName: String, titulo: String, texto: String): Boolean {
        val combined = "$packageName $titulo $texto".lowercase()
        if (BANK_PACKAGE_HINTS.any { packageName.contains(it, ignoreCase = true) }) return true
        return combined.contains("compra") ||
            (combined.contains("pix") && (combined.contains("valor") || combined.contains("r$")))
    }

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val component = ComponentName(context, NotificationReaderService::class.java)
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false
        return flat.split(":").any { it.equals(component.flattenToString(), ignoreCase = true) }
    }

    fun openNotificationListenerSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    private fun StringBuilder.appendPart(value: String?) {
        if (value.isNullOrBlank()) return
        if (isNotEmpty()) append(' ')
        append(value.trim())
    }
}
