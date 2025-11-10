package com.paulo.controle_gastos

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.paulo.controle_gastos.model.ExpenseParser

class NotificationReaderService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pacote = sbn.packageName ?: return
        val extras = sbn.notification.extras

        val titulo = extras.getString("android.title") ?: ""
        val texto = extras.getString("android.text") ?: ""

        Log.d("NotificationReader", "Notificação de $pacote: $titulo - $texto")

        // Apenas tenta processar notificações bancárias conhecidas
        if (pacote.contains("nubank", true) || pacote.contains("itau", true) || titulo.contains("compra", true)) {
            val despesa = ExpenseParser.parseMessage(titulo, texto, contaIdPadrao = "1")
            if (despesa != null) {
                Log.i("NotificationReader", "Despesa detectada via notificação: ${despesa.local} - R$${despesa.valor}")
                Toast.makeText(applicationContext, "Despesa detectada: ${despesa.local}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Opcional — você pode monitorar remoções se quiser
    }
}
