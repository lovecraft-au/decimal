package au.lovecraft.math.decimal

import kotlinx.serialization.KSerializer
import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSDecimalNumberHandler
import platform.Foundation.NSOrderedDescending
import platform.Foundation.NSRoundingMode

@Suppress("CONFLICTING_OVERLOADS") // ⚠️ Affects compilation, see: https://kotlinlang.org/docs/native-objc-interop.html#subclassing
actual typealias PlatformDecimal = NSDecimalNumber

actual value class Decimal private constructor(val value: PlatformDecimal) : Comparable<Decimal> {

    actual constructor(int: Int) : this(NSDecimalNumber(int))

    actual constructor(unsignedLong: ULong) : this(
        NSDecimalNumber.decimalNumberWithMantissa(
            mantissa = unsignedLong,
            exponent = 0,
            isNegative = false,
        ),
    )

    actual infix operator fun plus(other: Decimal) =
        Decimal(value.decimalNumberByAdding(other.value))

    actual infix operator fun minus(other: Decimal) =
        Decimal(value.decimalNumberBySubtracting(other.value))

    actual infix operator fun times(other: Decimal) =
        Decimal(value.decimalNumberByMultiplyingBy(other.value))

    actual infix operator fun div(other: Decimal) =
        Decimal(value.decimalNumberByDividingBy(other.value, generalRounding))

    actual fun isNegative(): Boolean = (NSDecimalNumber.zero.compare(value) == NSOrderedDescending)

    actual fun inverted(): Decimal = this * NegativeOne

    /**
     * More complicated than the JVM implementation since Foundation doesn't provide
     * an out-of-the box remainder/modulo function for [NSDecimalNumber].
     * This implementation adapted from: https://stackoverflow.com/a/15008969
     */
    actual infix operator fun rem(other: Decimal): Decimal {
        val quotientRoundingMode: NSRoundingMode =
            if (this.isNegative() xor other.isNegative()) NSRoundingMode.NSRoundUp else NSRoundingMode.NSRoundDown
        val quotientRounding: NSDecimalNumberHandler = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
            roundingMode = quotientRoundingMode,
            scale = 0,
            raiseOnExactness = false,
            raiseOnOverflow = false,
            raiseOnUnderflow = false,
            raiseOnDivideByZero = false,
        )
        // Divide and get the remainder
        val quotient: NSDecimalNumber = value.decimalNumberByDividingBy(
            decimalNumber = other.value,
            withBehavior = quotientRounding,
        )
        val subtract: NSDecimalNumber = quotient.decimalNumberByMultiplyingBy(decimalNumber = other.value)
        return Decimal(value.decimalNumberBySubtracting(subtract))
    }

    actual override infix operator fun compareTo(other: Decimal): Int = (value.compare(other.value)).toInt()

    actual fun movePointLeft(places: Int) = Decimal(value.decimalNumberByMultiplyingByPowerOf10((0 - places).toShort()))

    actual fun movePointRight(places: Int) = Decimal(value.decimalNumberByMultiplyingByPowerOf10(places.toShort()))

    actual override fun toString(): String = value.stringValue()

    actual fun truncate() =
        Decimal(
            value.decimalNumberByRoundingAccordingToBehavior(
                if (isNegative()) truncationRoundingUp else truncationRoundingDown,
            ),
        )

    actual fun toULong(): ULong = value.unsignedLongLongValue

    actual fun multiplyRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding,
    ) = Decimal(
        value.decimalNumberByMultiplyingBy(
            decimalNumber = other.value,
            withBehavior = handler(rounding, scale),
        ),
    )

    actual fun divideRounded(
        other: Decimal,
        scale: Short,
        rounding: Rounding,
    ) = Decimal(value.decimalNumberByDividingBy(decimalNumber = other.value, withBehavior = handler(rounding, scale)))

    actual fun rounded(scale: Short, rounding: Rounding): Decimal =
        Decimal(value.decimalNumberByRoundingAccordingToBehavior(handler(rounding, scale)))

    actual companion object {
        actual val Zero: Decimal get() = Decimal(NSDecimalNumber.zero)
        actual val One: Decimal get() = Decimal(NSDecimalNumber.one)

        actual val NegativeOne: Decimal
            get() = Decimal(
                NSDecimalNumber.decimalNumberWithMantissa(
                    mantissa = 1u,
                    exponent = 0,
                    isNegative = true,
                ),
            )

        internal fun handler(
            rounding: Rounding,
            scale: Short,
        ) = NSDecimalNumberHandler.decimalNumberHandlerWithRoundingMode(
            roundingMode = when (rounding) {
                Rounding.Up -> NSRoundingMode.NSRoundUp
                Rounding.Down -> NSRoundingMode.NSRoundDown
                Rounding.HalfUp -> NSRoundingMode.NSRoundPlain
                Rounding.HalfEven -> NSRoundingMode.NSRoundBankers
            },
            scale = scale,
            raiseOnExactness = false,
            raiseOnOverflow = false,
            raiseOnUnderflow = false,
            raiseOnDivideByZero = false,
        )

        private val generalRounding: NSDecimalNumberHandler = handler(
            rounding = Rounding.HalfUp,
            scale = GeneralDecimalRoundingScale.toShort(),
        )

        private val truncationRoundingDown: NSDecimalNumberHandler = handler(
            rounding = Rounding.Down,
            scale = 0,
        )

        private val truncationRoundingUp: NSDecimalNumberHandler = handler(
            rounding = Rounding.Up,
            scale = 0,
        )

        actual fun fromString(string: String): Decimal? {
            if (string.none { char -> char.isDigit() }) return null
            return runCatching {
                NSDecimalNumber.decimalNumberWithString(string)
                    .takeUnless { value -> value.isEqualToNumber(NSDecimalNumber.notANumber) }
            }.getOrNull()?.let(::Decimal)
        }

        actual fun from(decimal: PlatformDecimal) = Decimal(decimal)
        actual fun from(integer: Int) = Decimal(NSDecimalNumber(integer))

        actual fun serializer(): KSerializer<Decimal> = DecimalAsStringSerializer
    }

    actual fun toDouble(): Double = this.value.doubleValue()

    actual fun equals(other: Decimal): Boolean = this.value.isEqualToNumber(other.value)
}
