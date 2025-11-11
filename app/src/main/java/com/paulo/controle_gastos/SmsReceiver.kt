package com.paulo.controle_gastos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.paulo.controle_gastos.model.ExpenseParser

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {

            val bundle = intent.extras ?: return
            val format = bundle.getString("format")

            val pdus = bundle.getParcelableArray("pdus") ?: return

            try {
                for (pdu in pdus) {

                    val msg = SmsMessage.createFromPdu(pdu as ByteArray, format)

                    val remetente = msg.displayOriginatingAddress ?: ""
                    val mensagem = msg.displayMessageBody ?: ""

                    Log.i("SmsReceiver", "SMS recebido de: $remetente -> $mensagem")

                    // ✅ AGORA CORRETO, USANDO fromText()
                    val despesa = ExpenseParser.fromText(
                        texto = mensagem,
                        contaId = "1"
                    )

                    Log.i("Parser", "Despesa detectada: ${despesa.local} - R$ ${despesa.valor}")
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Erro ao processar SMS", e)
            }
        }
    }
}
