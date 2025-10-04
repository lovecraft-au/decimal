package au.lovecraft.math.decimal

import kotlin.jvm.JvmInline

/**
 * A percentage expressed as a [Decimal], where `1.00` is 100%.
 */
@JvmInline
value class Percent(val decimal: Decimal) {

    /**
     * [ISO 31-0](https://www.bipm.org/en/publications/si-brochure): 5.4.7 Stating quantity values being pure numbers
     *
     * > The internationally recognized symbol % (percent) may be used with the SI.
     * > When it is used, a space separates the number and the symbol %.
     */
    fun toIsoString(): String = "${decimal.movePointRight(2)} %"

    override fun toString(): String = toIsoString()

    companion object {
        val Zero: Percent = Percent(Decimal.Zero)
        val One: Percent = Percent(Decimal.One.movePointLeft(2))
        val Ten: Percent = Percent(Decimal.One.movePointLeft(1))
        val OneHundred: Percent = Percent(Decimal.One)

        /**
         * Convert a full/long representation of percentage points (i.e. 100.0 is 100%) to a more arithmetic-friendly [Percent].
         */
        fun fromPercentagePoints(value: Decimal): Percent = Percent(value.movePointLeft(2))
    }
}
