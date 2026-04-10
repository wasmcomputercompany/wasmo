package com.wasmo.support.absurd

import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class CancellationPolicySerializer : KSerializer<CancellationPolicy> {
  private val delegate = CancellationPolicyJson.serializer().nullable

  override val descriptor = SerialDescriptor("absurd.CancellationPolicy", delegate.descriptor)

  override fun serialize(encoder: Encoder, value: CancellationPolicy) {
    if (value.maxDuration == null && value.maxDelay == null) {
      encoder.encodeNull()
    } else {
      encoder.encodeSerializableValue(
        delegate,
        CancellationPolicyJson(
          max_duration = value.maxDuration?.inWholeSeconds?.toInt(),
          max_delay = value.maxDelay?.inWholeSeconds?.toInt(),
        ),
      )
    }
  }

  override fun deserialize(decoder: Decoder): CancellationPolicy {
    val value = decoder.decodeSerializableValue(delegate)
      ?: return CancellationPolicy()
    return CancellationPolicy(
      maxDuration = value.max_duration?.seconds,
      maxDelay = value.max_delay?.seconds,
    )
  }
}

@Serializable
internal class CancellationPolicyJson(
  val max_duration: Int? = null,
  val max_delay: Int? = null,
)

class RetryStrategySerializer : KSerializer<RetryStrategy> {
  private val delegate = RetryStrategyJson.serializer()

  override val descriptor = SerialDescriptor("absurd.RetryStrategy", delegate.descriptor)

  override fun serialize(
    encoder: Encoder,
    value: RetryStrategy,
  ) {
    encoder.encodeSerializableValue(
      delegate,
      RetryStrategyJson(
        kind = when {
          value.base == null -> "none"
          value.factor == 1f -> "fixed"
          else -> "exponential"
        },
        base_seconds = value.base?.inWholeSeconds?.toInt(),
        factor = value.factor,
        max_seconds = value.max?.inWholeSeconds?.toInt(),
      ),
    )
  }

  override fun deserialize(decoder: Decoder): RetryStrategy {
    val value = decoder.decodeSerializableValue(delegate)
    return RetryStrategy(
      base = value.base_seconds?.seconds,
      factor = value.factor ?: 1f,
      max = value.max_seconds?.seconds,
    )
  }
}

@Serializable
internal class RetryStrategyJson(
  val kind: String,
  val base_seconds: Int? = null,
  val factor: Float = 1f,
  val max_seconds: Int? = null,
)
