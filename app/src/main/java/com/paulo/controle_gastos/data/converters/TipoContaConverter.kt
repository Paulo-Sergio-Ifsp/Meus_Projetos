package com.paulo.controle_gastos.data.converters

import androidx.room.TypeConverter
import com.paulo.controle_gastos.model.TipoConta

class TipoContaConverter {

    @TypeConverter
    fun toTipoConta(value: String): TipoConta {
        return TipoConta.valueOf(value)
    }

    @TypeConverter
    fun fromTipoConta(tipo: TipoConta): String {
        return tipo.name
    }
}
