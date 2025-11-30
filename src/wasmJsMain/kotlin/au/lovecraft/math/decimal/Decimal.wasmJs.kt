package au.lovecraft.math.decimal

import kotlinx.serialization.KSerializer

actual typealias PlatformDecimal = DecimalJs

/**
 * Similarly to JVM...
 *
 * The wrapped operations here must aggressively make use of `stripTrailingZeros` as seen;
 * because whereas JVM's [BigDecimal] factors precision into its equality comparison, iOS
 * does not, and so we want a general [au.lovecraft.math.decimal.Decimal] comparison to compare only numerical value
 * and not precision.
 *
 * Ideally this would be achieved by overriding `equals` for [au.lovecraft.math.decimal.Decimal] and using comparison within
 * it, but this is not currently supported for a value class.
 *
 * The constructor is also private so that we can enforce the use of static [Companion.from] function
 * and thereby strip trailing zeroes at the point of creation.
 *
 * External declaration for decimal.js library
 * https://www.npmjs.com/package/decimal.js/v/10.6.0
 */
actual value class Decimal private actual constructor(actual val value: PlatformDecimal) : Comparable<Decimal> {

    actual infix operator fun plus(other: Decimal): Decimal =
        Decimal(this.value.plus(other.value).stripTrailingZeros())

    actual infix operator fun minus(other: Decimal): Decimal =
        Decimal(this.value.minus(other.value).stripTrailingZeros())

    actual infix operator fun times(other: Decimal): Decimal =
        Decimal(this.value.times(other.value).stripTrailingZeros())

    actual infix operator fun div(other: Decimal): Decimal =
        divideRounded(other, GeneralDecimalRoundingScale.toShort(), Rounding.HalfUp)

    actual infix operator fun rem(other: Decimal): Decimal =
        Decimal(this.value.modulo(other.value).stripTrailingZeros())

    actual override infix operator fun compareTo(other: Decimal): Int =
        this.value.comparedTo(other.value)

    actual fun movePointLeft(places: Int): Decimal =
        Decimal(this.value.dividedBy(DecimalJs(10).pow(DecimalJs(places))).stripTrailingZeros())

    actual fun movePointRight(places: Int): Decimal =
        Decimal(this.value.times(DecimalJs(10).pow(DecimalJs(places))).stripTrailingZeros())

    actual fun isNegative(): Boolean =
        this.value.isNegative()

    actual fun inverted(): Decimal =
        Decimal(this.value.negated())

    actual fun truncate(): Decimal =
        Decimal(this.value.truncated().stripTrailingZeros())

    actual override fun toString(): String =
        this.value.toString()

    actual fun toULong(): ULong =
        this.value.toNumber().toULong()

    actual fun toDouble(): Double =
        this.value.toNumber()

    actual fun multiplyRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding
    ): Decimal =
        Decimal(
            this.value.times(other.value).toDecimalPlaces(scale.toInt(), rounding.toDecimalJsRounding())
                .stripTrailingZeros()
        )

    actual fun divideRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding
    ): Decimal =
        Decimal(
            this.value.dividedBy(other.value).toDecimalPlaces(scale.toInt(), rounding.toDecimalJsRounding())
                .stripTrailingZeros()
        )

    actual fun rounded(
        scale: Short,
        rounding: Rounding
    ): Decimal =
        Decimal(this.value.toDecimalPlaces(scale.toInt(), rounding.toDecimalJsRounding()).stripTrailingZeros())

    actual companion object {
        actual val NegativeOne: Decimal =
            Decimal(DecimalJs("-1"))

        actual val Zero: Decimal
            get() = Decimal(DecimalJs("0"))

        actual val One: Decimal
            get() = Decimal(DecimalJs("1"))

        actual fun from(decimal: PlatformDecimal): Decimal =
            Decimal(decimal.stripTrailingZeros())

        actual fun from(integer: Int): Decimal =
            Decimal(DecimalJs(integer).stripTrailingZeros())

        actual fun fromString(string: String): Decimal? =
            try {
                Decimal(DecimalJs(string).stripTrailingZeros())
            } catch (e: Throwable) {
                null
            }

        actual fun serializer(): KSerializer<Decimal> = DecimalAsStringSerializer
    }

    actual constructor(int: Int) : this(DecimalJs(int).stripTrailingZeros())
    actual constructor(unsignedLong: ULong) : this(DecimalJs(unsignedLong.toString()).stripTrailingZeros())

    actual override fun equals(other: Any?): Boolean {
        if (other !is Decimal) return false
        return equals(other)
    }

    actual fun equals(other: Decimal): Boolean = value.equals(other.value)
}

// Extension function to convert Kotlin Rounding to decimal.js rounding mode
private fun Rounding.toDecimalJsRounding(): Int = when (this) {
    Rounding.Up -> DecimalJs.ROUND_UP
    Rounding.Down -> DecimalJs.ROUND_DOWN
    Rounding.HalfUp -> DecimalJs.ROUND_HALF_UP
    Rounding.HalfEven -> DecimalJs.ROUND_HALF_EVEN
    // Add more rounding modes as needed
}

private fun PlatformDecimal.stripTrailingZeros(): PlatformDecimal =
    DecimalJs(this.toString())
