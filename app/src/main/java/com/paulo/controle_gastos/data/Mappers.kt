package com.paulo.controle_gastos.data

import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho

/**
 * Mapeia da Entidade (Banco de Dados) para o Modelo (UI)
 */
fun ContaEntity.toModel(): Conta = Conta(id, nome, tipo, saldoInicial)
fun DespesaEntity.toModel(): Despesa = Despesa(id, data, local, valor, metodoPagamento, contaId)
fun GanhoEntity.toModel(): Ganho = Ganho(id, data, descricao, valor, contaId)


/**
 * Mapeia do Modelo (UI) para a Entidade (Banco de Dados)
 */
fun Conta.toEntity(): ContaEntity = ContaEntity(id, nome, tipo, saldoInicial)
fun Despesa.toEntity(): DespesaEntity = DespesaEntity(id, data, local, valor, metodoPagamento, contaId)
fun Ganho.toEntity(): GanhoEntity = GanhoEntity(id, data, descricao, valor, contaId)