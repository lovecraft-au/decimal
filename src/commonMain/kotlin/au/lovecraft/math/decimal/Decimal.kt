package au.lovecraft.math.decimal

expect class PlatformDecimal

const val GeneralDecimalRoundingScale: Int = 10

expect value class Decimal private constructor(val value: PlatformDecimal) : Comparable<Decimal> {

    constructor(int: Int)

    constructor(unsignedLong: ULong)

    infix operator fun plus(other: Decimal): Decimal

    infix operator fun minus(other: Decimal): Decimal

    infix operator fun times(other: Decimal): Decimal

    infix operator fun div(other: Decimal): Decimal

    infix operator fun rem(other: Decimal): Decimal

    override infix operator fun compareTo(other: Decimal): Int

    fun movePointLeft(places: Int): Decimal

    fun movePointRight(places: Int): Decimal

    fun isNegative(): Boolean

    fun inverted(): Decimal

    fun truncate(): Decimal

    override fun toString(): String

    fun multiplyRounded(other: Decimal, scale: Short, rounding: Rounding): Decimal

    fun divideRounded(other: Decimal, scale: Short, rounding: Rounding): Decimal

    fun rounded(scale: Short, rounding: Rounding): Decimal

    fun toULong(): ULong

    fun toDouble(): Double

    override fun equals(other: Any?): Boolean

    fun equals(other: Decimal): Boolean

    companion object {
        val Zero: Decimal
        val One: Decimal
        val NegativeOne: Decimal

        fun fromString(string: String): Decimal?
        fun from(decimal: PlatformDecimal): Decimal
        fun from(integer: Int): Decimal
    }
}
