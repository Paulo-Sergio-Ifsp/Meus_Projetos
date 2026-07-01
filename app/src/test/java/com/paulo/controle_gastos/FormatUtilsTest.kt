package com.paulo.controle_gastos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `parse pt-BR with thousands dot and comma decimal`() {
        val v = FormatUtils.parseUserDecimal("1.523,44")
        assertEquals(1523.44, v ?: 0.0, 0.0001)
    }

    @Test
    fun `parse pt-BR without thousands`() {
        val v = FormatUtils.parseUserDecimal("1523,44")
        assertEquals(1523.44, v ?: 0.0, 0.0001)
    }

    @Test
    fun `parse en-US format`() {
        val v = FormatUtils.parseUserDecimal("1523.44")
        assertEquals(1523.44, v ?: 0.0, 0.0001)
    }

    @Test
    fun `parse currency with symbol`() {
        val v = FormatUtils.parseUserDecimal("R$ 1.523,44")
        assertEquals(1523.44, v ?: 0.0, 0.0001)
    }

    @Test
    fun `invalid returns null`() {
        val v = FormatUtils.parseUserDecimal("abc")
        assertNull(v)
    }
}