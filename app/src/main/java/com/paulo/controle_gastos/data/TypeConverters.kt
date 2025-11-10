package com.paulo.controle_gastos.data

import androidx.room.TypeConverter
import com.paulo.controle_gastos.model.TipoConta

class TypeConverters {
    @TypeConverter
    fun fromTipoConta(tipo: TipoConta): String {
        return tipo.name // Salva o Enum como String (ex: "CARTAO_CREDITO")
    }

    @TypeConverter
    fun toTipoConta(tipo: String): TipoConta {
        return TipoConta.valueOf(tipo) // Converte a String de volta para o Enum
    }
}