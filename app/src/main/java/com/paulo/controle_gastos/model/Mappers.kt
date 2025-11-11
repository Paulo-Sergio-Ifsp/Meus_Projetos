package com.paulo.controle_gastos.data

import com.paulo.controle_gastos.data.entity.ContaEntity
import com.paulo.controle_gastos.data.entity.DespesaEntity
import com.paulo.controle_gastos.data.entity.GanhoEntity
import com.paulo.controle_gastos.model.Conta
import com.paulo.controle_gastos.model.Despesa
import com.paulo.controle_gastos.model.Ganho

fun ContaEntity.toModel() = Conta(
    id = id,
    nome = nome,
    tipo = tipo,
    saldoInicial = saldoInicial
)

fun Conta.toEntity() = ContaEntity(
    id = id,
    nome = nome,
    tipo = tipo,
    saldoInicial = saldoInicial
)

fun DespesaEntity.toModel() = Despesa(
    id = id,
    data = data,
    local = local,
    valor = valor,
    metodoPagamento = metodoPagamento,
    contaId = contaId
)

fun Despesa.toEntity() = DespesaEntity(
    id = id,
    data = data,
    local = local,
    valor = valor,
    metodoPagamento = metodoPagamento,
    contaId = contaId
)

fun GanhoEntity.toModel() = Ganho(
    id = id,
    data = data,
    descricao = descricao,
    valor = valor,
    contaId = contaId
)

fun Ganho.toEntity() = GanhoEntity(
    id = id,
    data = data,
    descricao = descricao,
    valor = valor,
    contaId = contaId
)
