package com.paulo.controle_gastos

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.paulo.controle_gastos.util.ExpenseCaptureHelper
import com.paulo.controle_gastos.util.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationReaderService : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pacote = sbn.packageName ?: return
        val (titulo, texto) = NotificationUtils.extractText(sbn.notification.extras)

        Log.d(TAG, "Notificação de $pacote: $titulo - $texto")

        if (!NotificationUtils.isBankRelated(pacote, titulo, texto)) return

        scope.launch {
            val despesa = ExpenseCaptureHelper.processAndPersist(
                context = applicationContext,
                titulo = titulo,
                texto = texto,
                packageName = pacote
            )
            if (despesa != null) {
                Log.i(TAG, "Despesa salva via notificação: ${despesa.local} - R$${despesa.valor}")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = Unit

    companion object {
        private const val TAG = "NotificationReader"
    }
}
