@file:OptIn(ExperimentalWasmJsInterop::class)

package au.lovecraft.math.decimal

@JsModule("decimal.js")
@JsName("Decimal")
external class DecimalJs {

    constructor(value: String)
    constructor(value: Int)
    constructor(value: Double)
    constructor(value: DecimalJs)

    // Arithmetic operations
    fun plus(other: DecimalJs): DecimalJs
    fun minus(other: DecimalJs): DecimalJs
    fun times(other: DecimalJs): DecimalJs
    fun dividedBy(other: DecimalJs): DecimalJs
    fun modulo(other: DecimalJs): DecimalJs

    fun pow(other: DecimalJs): DecimalJs

    // Comparison
    fun comparedTo(other: DecimalJs): Int
    fun equals(other: DecimalJs): Boolean
    fun greaterThan(other: DecimalJs): Boolean
    fun lessThan(other: DecimalJs): Boolean

    // Utility methods
    fun isNegative(): Boolean
    fun isZero(): Boolean
    fun isPositive(): Boolean
    fun abs(): DecimalJs
    fun negated(): DecimalJs
    fun truncated(): DecimalJs

    // String conversion
    fun toFixed(decimalPlaces: Int): String
    fun toNumber(): Double

    // Rounding
    fun toDecimalPlaces(decimalPlaces: Int, roundingMode: Int = definedExternally): DecimalJs
    fun round(): DecimalJs

    // Static methods
    companion object {
        fun set(config: JsAny): DecimalJs
        fun clone(config: JsAny = definedExternally): DecimalJs
        fun isDecimal(value: JsAny): Boolean
        fun max(vararg values: DecimalJs): DecimalJs
        fun min(vararg values: DecimalJs): DecimalJs
        fun random(decimalPlaces: Int = definedExternally): DecimalJs

        // Static constants
        val ROUND_UP: Int
        val ROUND_DOWN: Int
        val ROUND_CEIL: Int
        val ROUND_FLOOR: Int
        val ROUND_HALF_UP: Int
        val ROUND_HALF_DOWN: Int
        val ROUND_HALF_EVEN: Int
        val ROUND_HALF_CEIL: Int
        val ROUND_HALF_FLOOR: Int
    }
}