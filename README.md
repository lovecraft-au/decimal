# Decimal (Kotlin Multiplatform)

A tiny Kotlin Multiplatform library that provides a common abstraction over each platform’s native “true decimal” type:

- JVM/Android: java.math.BigDecimal
- iOS (Kotlin/Native): Foundation.NSDecimalNumber
- WebAssembly/JS: decimal.js (via npm)

Use it anywhere you need exact base‑10 arithmetic (money, percentages, rates) without floating‑point surprises, and keep one API across all targets.


## Why another decimal?

- True decimal arithmetic across platforms: wraps the most native decimal implementation available on each target.
- Consistent semantics: JVM’s BigDecimal equality depends on scale (precision). Other platforms don’t. This library “normalizes” values by stripping trailing zeros during construction and after operations so that 1.0 and 1.00 compare equal everywhere.
- Small surface area: a single value class Decimal with operators, conversions, and explicit rounding operations.


## Supported targets

Configured in this module’s Gradle build:

- Android (multiplatform Android target)
- JVM
- iOS (arm64 device and simulator)
- Wasm JS (browser; backed by decimal.js)

Kotlin version: 2.2.20


## Installation

This library is not yet published to a public repository. Recommended ways to consume it today:

- Include as a Git submodule and reference the project directly, or
- Use a Gradle included build for local development.

Example using an included build:

settings.gradle.kts in your app project:

```
includeBuild("../decimal") // path where this repo lives
```

Then in your module’s build.gradle.kts:

```
dependencies {
    implementation(project(":decimal"))
}
```

Wasm JS target note: the library depends on npm package decimal.js 10.6.0; your build will fetch it automatically.


## Quick start

```kotlin
// Construct
val a = Decimal(42)                 // from Int
val b = Decimal(1234uL)             // from ULong
val c = Decimal.fromString("12.50") // nullable

// Arithmetic (operators return Decimal)
val sum = a + Decimal.from(1)
val product = sum * Decimal.from(3)

// Division uses a general default (scale=10, HalfUp)
val quotient = product / Decimal.from(7)

// Explicit rounding
val rounded = quotient.rounded(scale = 2, rounding = Rounding.HalfEven)

// Move the decimal point
val dollars = Decimal(12345).movePointLeft(2)  // 123.45
val cents = dollars.movePointRight(2)          // 12345

// Sign helpers and truncation
val negative = dollars.inverted()
val truncated = dollars.truncate()             // drop fractional part

// Conversions
val asULong = cents.toULong()
val asDouble = dollars.toDouble()
```


## Rounding

Rounding defines the strategy used in explicit rounding operations:

- Up
- Down
- HalfUp
- HalfEven (aka “Banker’s rounding”)

APIs:

- multiplyRounded(other, scale, rounding)
- divideRounded(other, scale, rounding)
- rounded(scale, rounding)

Note on division: the infix div operator (/) uses a general default of scale = 10 and rounding = HalfUp for convenience and cross‑platform consistency.


## Equality and precision semantics

- On JVM, BigDecimal(1.0) != BigDecimal(1.00) by default. To make equality and comparisons consistent across targets, the Decimal wrapper strips trailing zeros during construction and after each operation.
- Compare values using standard Kotlin comparison operators; Decimal implements Comparable.
- toString() yields a plain, human‑readable representation (no scientific notation, no spurious trailing zeros).


## Percent

A small helper that expresses percentages as a Decimal where 1.00 is 100%.

```kotlin
val fivePercent = Percent.fromPercentagePoints(Decimal.from(5))  // 5% = 0.05
val tenPercent = Percent.Ten                                     // 10% = 0.10
val hundred = Percent.OneHundred                                 // 100% = 1.00

println(fivePercent.toIsoString()) // e.g. "5 %" (ISO 31-0 spacing)
```

Constants:

- Percent.Zero (0%)
- Percent.One (1%)
- Percent.Ten (10%)
- Percent.OneHundred (100%)


## CurrencyAmount (optional finance helpers)

A thin value class over Decimal for representing non‑negative currency amounts with two fractional digits. Construction coerces to two decimal places using Bankers’ rounding to reduce drift.

Key points:

- Always non‑negative; constructing with a negative Decimal will error.
- Two decimal places are enforced on construction/rounding.
- toCents() returns an unsigned long cent value.

Common APIs:

```kotlin
val amount = CurrencyAmount.fromString("12.345")!!   // -> 12.35 (HalfEven)
val cents: ULong = amount.toCents()                   // 1235uL

val plusFee = amount + CurrencyAmount.fromDecimal(Decimal.from(1))
val discounted = amount * Percent.Ten                 // 10% of amount

// Multi‑step arithmetic without compounding rounding at each step
val result = amount.mapDecimal { d ->
    ((d * Decimal(3)) + Decimal(1)) / Decimal(2)
}

// Locale‑aware formatting (currently AU locale in this repo)
val s1 = amount.formatted(withSymbol = true)          // e.g. "$12.35"
val s2 = amount.formatted(withSymbol = false)         // e.g. "12.35"
```

Additional helpers:

- "123.45".dollars and 123.dollars extension properties
- formattedOrFree(withSymbol: Boolean = true, free: String = "free")

Platform notes:

- JVM/Android uses java.text.NumberFormat with Locale("en", "AU").
- iOS uses NSNumberFormatter with currencyCode = "AU". Adjust as needed for your market.


## Platform backends

- JVM/Android: wraps BigDecimal.
- iOS: wraps NSDecimalNumber and uses NSDecimalNumberHandler for rounding behaviors.
- Wasm JS: wraps decimal.js, mapping Rounding to the library’s rounding modes.


## Building

- Requires JDK 21 for JVM/Android (configured via jvmToolchain(21)).
- Wasm JS target pulls decimal.js automatically.

Commands:

```
./gradlew build
```


## Roadmap

- Publish artifacts to a public repository
- Expand platform coverage if/when needed
- Configurable locale/currency for formatting helpers


## License

This software is released under the LGPL License.
See [LICENSE.md](LICENSE.md) for details.
