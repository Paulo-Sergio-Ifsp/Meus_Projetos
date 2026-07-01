package com.paulo.controle_gastos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.paulo.controle_gastos.util.ExpenseCaptureHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val bundle = intent.extras ?: return
        val format = bundle.getString("format")
        val pdus = bundle.get("pdus") as? Array<*> ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                for (pdu in pdus) {
                    val msg = SmsMessage.createFromPdu(pdu as ByteArray, format)
                    val remetente = msg.displayOriginatingAddress ?: ""
                    val mensagem = msg.displayMessageBody ?: ""

                    Log.i(TAG, "SMS recebido de: $remetente -> $mensagem")
                    processMessageSuspend(context, remetente, mensagem)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao processar SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    suspend fun processMessageSuspend(context: Context, remetente: String, mensagem: String) {
        ExpenseCaptureHelper.processAndPersist(context, remetente, mensagem)
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
