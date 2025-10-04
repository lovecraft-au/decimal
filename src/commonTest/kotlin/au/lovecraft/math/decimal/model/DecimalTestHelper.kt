package au.lovecraft.math.decimal.model

import au.lovecraft.math.decimal.Decimal
import au.lovecraft.math.decimal.Percent

fun String.asDecimal(): Decimal = requireNotNull(asDecimalOrNull()) {
    "Invalid test decimal value '$this'"
}

fun String.asDecimalOrNull(): Decimal? = Decimal.fromString(this)

fun String.asPercentagePoints(): Percent = Percent.fromPercentagePoints(asDecimal())
