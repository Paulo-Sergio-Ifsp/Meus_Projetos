package com.paulo.controle_gastos

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.TipoConta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SmsReceiverIntegrationTest {

    private lateinit var app: FinanceApp

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext() as FinanceApp
        runBlocking { app.database.clearAllTables() }
    }

    @Test
    fun smsProcessing_insertsDespesa() = runBlocking {
        app.repository.addConta(
            Conta(
                id = UUID.randomUUID().toString(),
                nome = "Nubank",
                tipo = TipoConta.CARTAO_CREDITO,
                saldoInicial = 0.0
            )
        )

        val receiver = SmsReceiver()
        val titulo = "NUBANK"
        val texto = "Compra aprovada no NUBANK R$ 15,50 em LOJA TESTE."

        receiver.processMessageSuspend(app, titulo, texto)

        val despesas = app.repository.despesas.first()

        assertTrue(
            despesas.any {
                it.local.lowercase().contains("loja teste") &&
                    kotlin.math.abs(it.valor - 15.50) < 0.001
            }
        )
    }
}
