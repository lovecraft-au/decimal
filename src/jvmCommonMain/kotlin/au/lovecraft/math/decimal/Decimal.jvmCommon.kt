package au.lovecraft.math.decimal

import kotlinx.serialization.KSerializer
import java.math.BigDecimal
import java.math.RoundingMode

actual typealias PlatformDecimal = BigDecimal

/**
 * The wrapped operations here must aggressively make use of `stripTrailingZeros` as seen;
 * because whereas JVM's [BigDecimal] factors precision into its equality comparison, other
 * platforms do not, and so we want a general [Decimal] comparison to compare only numerical value
 * and not precision.
 *
 * Ideally this would be achieved by overriding `equals` for JVM [Decimal] and using comparison within
 * it, but this is not currently supported for a value class.
 *
 * The constructor is also private so that we can enforce the use of static [Companion.from] function
 * and thereby strip trailing zeroes at the point of creation.
 */
@JvmInline
actual value class Decimal private constructor(val value: PlatformDecimal) : Comparable<Decimal> {

    actual constructor(int: Int) : this(BigDecimal(int).stripTrailingZeros())

    actual constructor(unsignedLong: ULong) : this(
        BigDecimal(unsignedLong.toString()).stripTrailingZeros(),
    )

    actual infix operator fun plus(other: Decimal) = Decimal((value + other.value).stripTrailingZeros())

    actual infix operator fun minus(other: Decimal) = Decimal((value - other.value).stripTrailingZeros())

    actual infix operator fun times(other: Decimal) = Decimal((value * other.value).stripTrailingZeros())

    actual infix operator fun div(other: Decimal) =
        divideRounded(other, GeneralDecimalRoundingScale.toShort(), Rounding.HalfUp)

    actual infix operator fun rem(other: Decimal) = Decimal((value % other.value).stripTrailingZeros())

    actual override infix operator fun compareTo(other: Decimal): Int = value.compareTo(other.value)

    actual fun movePointLeft(places: Int) = Decimal(value.movePointLeft(places).stripTrailingZeros())

    actual fun movePointRight(places: Int) = Decimal(value.movePointRight(places).stripTrailingZeros())

    actual fun isNegative() = value < BigDecimal.ZERO

    actual fun inverted() = Decimal(value.negate())

    /** Uses [BigDecimal.stripTrailingZeros] to match behaviour of iOS platform */
    actual override fun toString(): String = value.toPlainString()

    actual fun truncate() = Decimal(value.setScale(0, RoundingMode.DOWN))

    actual fun toULong(): ULong = toString().toULong()

    actual fun toDouble(): Double = value.toDouble()

    actual fun multiplyRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding,
    ) = Decimal(
        (value * other.value).setScale(
            scale.toInt(),
            platformRounding(rounding)
        ).stripTrailingZeros()
    )

    actual fun divideRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding,
    ) = Decimal(
        value.divide(
            other.value, scale.toInt(),
            platformRounding(rounding)
        ).stripTrailingZeros()
    )

    actual fun rounded(
        scale: Short,
        rounding: Rounding,
    ) = Decimal(value.setScale(scale.toInt(), platformRounding(rounding)).stripTrailingZeros())

    actual companion object {
        actual val Zero: Decimal get() = Decimal(BigDecimal.ZERO)
        actual val One: Decimal get() = Decimal(BigDecimal.ONE)
        actual val NegativeOne: Decimal get() = Decimal(BigDecimal.valueOf(-1))
        actual fun fromString(string: String): Decimal? = try {
            Decimal(BigDecimal(string).stripTrailingZeros())
        } catch (_: Throwable) {
            null
        }

        actual fun from(decimal: PlatformDecimal) = Decimal(decimal.stripTrailingZeros())
        actual fun from(integer: Int) = Decimal(BigDecimal(integer).stripTrailingZeros())

        actual fun serializer(): KSerializer<Decimal> = DecimalAsStringSerializer
    }

    actual fun equals(other: Decimal): Boolean = value.compareTo(other.value) == 0
}

private fun platformRounding(
    rounding: Rounding,
): RoundingMode = when (rounding) {
    Rounding.Up -> RoundingMode.UP
    Rounding.Down -> RoundingMode.DOWN
    Rounding.HalfUp -> RoundingMode.HALF_UP
    Rounding.HalfEven -> RoundingMode.HALF_EVEN
}
