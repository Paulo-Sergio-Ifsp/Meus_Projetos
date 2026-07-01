package com.paulo.controle_gastos.util

import android.content.Context
import android.util.Log
import com.paulo.controle_gastos.FinanceApp
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.ExpenseParser
import kotlinx.coroutines.flow.first

object ExpenseCaptureHelper {

    private const val TAG = "ExpenseCaptureHelper"

    suspend fun processAndPersist(
        context: Context,
        titulo: String,
        texto: String,
        packageName: String? = null
    ): Despesa? {
        val app = context.applicationContext as? FinanceApp
        if (app == null) {
            Log.e(TAG, "ApplicationContext não é FinanceApp")
            return null
        }

        val contas = app.repository.contas.first()
        if (contas.isEmpty()) {
            Log.w(TAG, "Nenhuma conta cadastrada — despesa não salva")
            return null
        }

        val draft = ExpenseParser.parseMessage(titulo, texto, contas.first().id)
        if (draft == null) {
            Log.d(TAG, "Mensagem não representou uma despesa")
            return null
        }

        val contaId = ContaResolver.resolve(
            contas = contas,
            titulo = titulo,
            texto = texto,
            packageName = packageName,
            metodoPagamento = draft.metodoPagamento
        ) ?: return null

        val despesa = draft.copy(contaId = contaId)
        app.repository.addDespesa(despesa)
        Log.i(TAG, "Despesa persistida: ${despesa.local} - R$${despesa.valor} (conta $contaId)")
        return despesa
    }
}
