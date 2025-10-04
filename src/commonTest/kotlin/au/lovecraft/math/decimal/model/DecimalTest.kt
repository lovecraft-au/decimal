package au.lovecraft.math.decimal.model

import au.lovecraft.math.decimal.Decimal
import au.lovecraft.math.decimal.Rounding
import au.lovecraft.math.decimal.test.shouldBe
import au.lovecraft.math.decimal.test.shouldBeNull
import kotlin.test.Test

class DecimalTest {

    @Test
    fun testCommuteFromToString() {
        "0".asDecimal().toString() shouldBe "0"
        "0.01".asDecimal().toString() shouldBe "0.01"
        "-0.01".asDecimal().toString() shouldBe "-0.01"
    }

    @Test
    fun testAdd() {
        ("1.5".asDecimal() + "1.5".asDecimal()).toString() shouldBe "3"
        ("-1".asDecimal() + "1".asDecimal()).toString() shouldBe "0"
        ("-1".asDecimal() + "1".asDecimal()).toString() shouldBe "0"
        ("123.321".asDecimal() + "0.0001".asDecimal()).toString() shouldBe "123.3211"
    }

    @Test
    fun testSubtract() {
        ("-3".asDecimal() - "-3".asDecimal()).toString() shouldBe "0"
        ("0".asDecimal() - "0".asDecimal()).toString() shouldBe "0"
    }

    @Test
    fun testMovePointLeft() {
        "1".asDecimal().movePointLeft(0).toString() shouldBe "1"
        "1".asDecimal().movePointLeft(1).toString() shouldBe "0.1"

        "9.9".asDecimal().movePointLeft(0).toString() shouldBe "9.9"
        "9.9".asDecimal().movePointLeft(1).toString() shouldBe "0.99"
        "9.9".asDecimal().movePointLeft(2).toString() shouldBe "0.099"
    }

    @Test
    fun testMovePointRight() {
        "1".asDecimal().movePointRight(0).toString() shouldBe "1"
        "1".asDecimal().movePointRight(1).toString() shouldBe "10"

        "9.9".asDecimal().movePointRight(0).toString() shouldBe "9.9"
        "9.9".asDecimal().movePointRight(1).toString() shouldBe "99"
        "9.9".asDecimal().movePointRight(2).toString() shouldBe "990"
    }

    @Test
    fun testInvert() {
        "0".asDecimal().inverted().toString() shouldBe "0"
        "1".asDecimal().inverted().toString() shouldBe "-1"
        "-1".asDecimal().inverted().toString() shouldBe "1"
    }

    @Test
    fun testIsNegative() {
        "1".asDecimal().isNegative() shouldBe false
        "0".asDecimal().isNegative() shouldBe false
        "-1".asDecimal().isNegative() shouldBe true
    }

    @Test
    fun testRemainder() {
        ("20.32".asDecimal() % "8".asDecimal()).toString() shouldBe "4.32"
        ("20.32".asDecimal() % "-8".asDecimal()).toString() shouldBe "4.32"
        ("-20.32".asDecimal() % "8".asDecimal()).toString() shouldBe "-4.32"
        ("-20.32".asDecimal() % "-8".asDecimal()).toString() shouldBe "-4.32"
    }

    @Test
    fun testCompareTo() {
        ("10.00".asDecimal() > "10.01".asDecimal()) shouldBe false
        ("10.01".asDecimal() > "10.01".asDecimal()) shouldBe false
        ("10.01".asDecimal() > "10.00".asDecimal()) shouldBe true

        ("10.00".asDecimal() < "10.01".asDecimal()) shouldBe true
        ("10.01".asDecimal() < "10.01".asDecimal()) shouldBe false
        ("10.01".asDecimal() < "10.00".asDecimal()) shouldBe false

        ("10.00".asDecimal() >= "10.01".asDecimal()) shouldBe false
        ("10.01".asDecimal() >= "10.01".asDecimal()) shouldBe true
        ("10.01".asDecimal() >= "10.00".asDecimal()) shouldBe true

        ("10.00".asDecimal() <= "10.01".asDecimal()) shouldBe true
        ("10.01".asDecimal() <= "10.01".asDecimal()) shouldBe true
        ("10.01".asDecimal() <= "10.00".asDecimal()) shouldBe false
    }

    /**
     * Maintain expected value if adjusting [GeneralDecimalRoundingScale].
     */
    @Test
    fun testDivision() {
        ("1".asDecimal() / "3".asDecimal()).toString() shouldBe "0.3333333333"
    }

    @Test
    fun testMultiplyRounded() {
        "1".asDecimal().multiplyRounded("0.33333333333333333333".asDecimal(), scale = 5, rounding = Rounding.Down)
            .toString() shouldBe "0.33333"
        "1".asDecimal().multiplyRounded("0.33333333333333333333".asDecimal(), scale = 5, rounding = Rounding.Up)
            .toString() shouldBe "0.33334"
    }

    @Test
    fun `Non-numeric strings are parsed as null`() {
        "".asDecimalOrNull().shouldBeNull()
        ".".asDecimalOrNull().shouldBeNull()
        " ".asDecimalOrNull().shouldBeNull()
        "A".asDecimalOrNull().shouldBeNull()
    }

    @Test
    fun `Test truncation`() {
        "3.999999".asDecimal().truncate().toString() shouldBe "3"
        "0.0000001".asDecimal().truncate().toString() shouldBe "0"
        "-9.99".asDecimal().truncate().toString() shouldBe "-9"
    }

    @Test
    fun `Test equality with differing precision`() {
        Decimal.fromString("1.0") shouldBe Decimal.fromString("1")
        Decimal.fromString("0.10") shouldBe Decimal.fromString("0.1")
        Decimal.fromString("-0.10") shouldBe Decimal.fromString("-0.1")
        Decimal.fromString("1000.00") shouldBe Decimal.fromString("1000")
    }
}
