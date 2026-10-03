package com.octomid.trisbraind.domain.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UniformityTestTest {

    @Test
    fun chiCuadradoCeroParaConteosUniformes() {
        val counts = IntArray(10) { 100 }
        assertEquals(0.0, UniformityTest.chiSquare(counts), 1e-9)
        assertFalse(UniformityTest.isDeviant(counts))
        assertEquals(1.0, UniformityTest.pValue(0.0), 1e-6)
    }

    @Test
    fun detectaDesviacionFuerte() {
        val counts = intArrayOf(300, 100, 100, 100, 100, 100, 100, 100, 100, 100)
        assertTrue(UniformityTest.isDeviant(counts))
        val p = UniformityTest.pValue(UniformityTest.chiSquare(counts))
        assertTrue("p=$p", p < 0.001)
    }

    @Test
    fun pValorBajoParaChiCuadradoGrande() {
        // A 16.919 (df=9) el p-valor debe estar cerca de 0.05.
        val p = UniformityTest.pValue(UniformityTest.CRITICAL_05)
        assertTrue("p=$p", p in 0.03..0.07)
    }
}
