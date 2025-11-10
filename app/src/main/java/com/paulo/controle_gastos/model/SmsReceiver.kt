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
            val format = bundle.getString("format")   // novo parâmetro necessário

            // ✅ forma correta moderna (sem deprecated)
            val pdus = bundle.getParcelableArray("pdus")?: return

            try {
                for (pdu in pdus) {

                    // ✅ createFromPdu correto para API 23+
                    val msg = SmsMessage.createFromPdu(pdu as ByteArray, format)

                    val remetente = msg.displayOriginatingAddress ?: ""
                    val mensagem = msg.displayMessageBody ?: ""

                    Log.i("SmsReceiver", "SMS recebido de: $remetente -> $mensagem")

                    val despesa = ExpenseParser.parseMessage(
                        remetente = remetente,
                        mensagem = mensagem,
                        contaIdPadrao = "1"
                    )

                    if (despesa != null) {
                        Log.i("Parser", "Despesa detectada: ${despesa.local} - R$ ${despesa.valor}")
                    } else {
                        Log.w("Parser", "Mensagem ignorada ou formato não reconhecido.")
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Erro ao processar SMS", e)
            }
        }
    }
}
