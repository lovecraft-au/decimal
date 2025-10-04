package au.lovecraft.math.decimal.test

import kotlin.test.assertEquals
import kotlin.test.assertNull

// These are duplicated in Decimal - make a common Utils project with Core/Test modules?

infix fun <T> T.shouldBe(expected: T) {
    assertEquals(expected, this)
}

fun <T> T?.shouldBeNull() {
    assertNull(this)
}
