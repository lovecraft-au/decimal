package au.lovecraft.math.decimal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * kotlinx.serialization serializer for [Decimal], encoding as a JSON string using `toString()`
 * and decoding via `Decimal.fromString(...)`.
 */
object DecimalAsStringSerializer : KSerializer<Decimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("au.lovecraft.math.decimal.Decimal", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Decimal) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Decimal {
        val str = decoder.decodeString()
        return Decimal.fromString(str)
            ?: throw SerializationException("Invalid Decimal string: '$str'")
    }
}
