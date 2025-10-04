package au.lovecraft.math.decimal.model

import au.lovecraft.math.decimal.Decimal
import au.lovecraft.math.decimal.Percent
import kotlin.test.Test
import kotlin.test.assertEquals

class PercentTest {

    @Test
    fun `given constant Percent values - they are scaled correctly`() {
        assertEquals("0.00".asDecimal(), Percent.Zero.decimal)
        assertEquals("0.01".asDecimal(), Percent.One.decimal)
        assertEquals("0.1".asDecimal(), Percent.Ten.decimal)
        assertEquals("1".asDecimal(), Percent.OneHundred.decimal)
    }

    @Test
    fun `given percentage points - when converted to a Percent - they are scaled correctly`() {
        assertEquals(Percent.Zero, Percent.fromPercentagePoints(Decimal.Zero))
        assertEquals(Percent.One, Percent.fromPercentagePoints(Decimal.from(1)))
        assertEquals(Percent.Ten, Percent.fromPercentagePoints(Decimal.from(10)))
        assertEquals(Percent.OneHundred, Percent.fromPercentagePoints(Decimal.from(100)))
    }

    @Test
    fun `given percent value - format to string as expected`() {
        assertEquals("0 %", Percent.Zero.toString())
        assertEquals("1 %", Percent.One.toString())
        assertEquals("10 %", Percent.Ten.toString())
        assertEquals("100 %", Percent.OneHundred.toString())

        assertEquals("0.1 %", Percent("0.001".asDecimal()).toString())
        assertEquals("0.11 %", Percent("0.0011".asDecimal()).toString())
        assertEquals("0.009 %", Percent("0.00009".asDecimal()).toString())
    }
}
