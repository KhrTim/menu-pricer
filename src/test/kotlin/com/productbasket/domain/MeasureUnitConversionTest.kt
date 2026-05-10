package com.productbasket.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MeasureUnitConversionTest {

    @Test fun `kg to kg identity`() = assertEquals(5.0, MeasureUnit.Kilogram.convertTo(5.0, MeasureUnit.Kilogram))
    @Test fun `kg to gram`() = assertEquals(1000.0, MeasureUnit.Kilogram.convertTo(1.0, MeasureUnit.Gram), 1e-9)
    @Test fun `gram to kg`() = assertEquals(0.2, MeasureUnit.Gram.convertTo(200.0, MeasureUnit.Kilogram), 1e-9)
    @Test fun `gram round trip`() {
        val kg = MeasureUnit.Gram.convertTo(500.0, MeasureUnit.Kilogram)
        val back = MeasureUnit.Kilogram.convertTo(kg, MeasureUnit.Gram)
        assertEquals(500.0, back, 1e-9)
    }

    @Test fun `liter to milliliter`() = assertEquals(1000.0, MeasureUnit.Liter.convertTo(1.0, MeasureUnit.Milliliter), 1e-9)
    @Test fun `milliliter to liter`() = assertEquals(0.25, MeasureUnit.Liter.convertTo(0.25, MeasureUnit.Liter), 1e-9)
    @Test fun `ml round trip`() {
        val l = MeasureUnit.Milliliter.convertTo(250.0, MeasureUnit.Liter)
        val back = MeasureUnit.Liter.convertTo(l, MeasureUnit.Milliliter)
        assertEquals(250.0, back, 1e-9)
    }

    @Test fun `piece identity`() = assertEquals(2.0, MeasureUnit.Piece.convertTo(2.0, MeasureUnit.Piece))

    @Test fun `weight to volume throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            MeasureUnit.Kilogram.convertTo(1.0, MeasureUnit.Liter)
        }
    }
    @Test fun `weight to countable throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            MeasureUnit.Gram.convertTo(1.0, MeasureUnit.Piece)
        }
    }
    @Test fun `volume to countable throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            MeasureUnit.Liter.convertTo(1.0, MeasureUnit.Piece)
        }
    }

    @Test fun `forCategory weight contains kg and gram only`() {
        val units = MeasureUnit.forCategory(UnitCategory.Weight)
        assertTrue(MeasureUnit.Kilogram in units)
        assertTrue(MeasureUnit.Gram in units)
        assertFalse(MeasureUnit.Liter in units)
        assertFalse(MeasureUnit.Piece in units)
    }
}
