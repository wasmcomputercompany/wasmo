package com.wasmo.support.absurd

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class JsonEncodingTest {
  private val PrettyPrintingJson = Json(KotlinJson) {
    prettyPrint = true
  }

  @Test
  fun retryStrategy() {
    checkRoundTrip(
      RetryStrategy(
        base = 3.seconds,
        factor = 1.5f,
        max = 10.seconds,
      ),
      """
      |{
      |    "kind": "exponential",
      |    "base_seconds": 3,
      |    "factor": 1.5,
      |    "max_seconds": 10
      |}
      """.trimMargin(),
    )

    checkRoundTrip(
      RetryStrategy(
        base = 3.seconds,
      ),
      """
      |{
      |    "kind": "fixed",
      |    "base_seconds": 3
      |}
      """.trimMargin(),
    )

    checkRoundTrip(
      RetryStrategy(),
      """
      |{
      |    "kind": "none"
      |}
      """.trimMargin(),
    )
  }

  @Test
  fun cancellationPolicy() {
    checkRoundTrip(
      CancellationPolicy(
        maxDuration = 4.seconds,
        maxDelay = 10.seconds,
      ),
      """
      |{
      |    "max_duration": 4,
      |    "max_delay": 10
      |}
      """.trimMargin(),
    )

    checkRoundTrip(
      CancellationPolicy(),
      "null",
    )
  }

  @Test
  fun spawnOptions() {
    checkRoundTrip(
      SpawnOptions(
        headers = null,
        max_attempts = null,
        retry_strategy = null,
        cancellation = null,
        idempotency_key = null,
      ),
      "{}",
    )

    checkRoundTrip(
      SpawnOptions(
        headers = Headers(),
        max_attempts = 3,
        retry_strategy = RetryStrategy(
          base = 3.seconds,
          factor = 1.5f,
          max = 10.seconds,
        ),
        cancellation = CancellationPolicy(
          4.seconds,
          10.seconds,
        ),
        idempotency_key = "only-once?!",
      ),
      """
      |{
      |    "headers": {},
      |    "max_attempts": 3,
      |    "retry_strategy": {
      |        "kind": "exponential",
      |        "base_seconds": 3,
      |        "factor": 1.5,
      |        "max_seconds": 10
      |    },
      |    "cancellation": {
      |        "max_duration": 4,
      |        "max_delay": 10
      |    },
      |    "idempotency_key": "only-once?!"
      |}
      """.trimMargin(),
    )
  }

  internal inline fun <reified T> checkRoundTrip(value: T, json: String) {
    val serializer = serializer<T>()
    assertThat(PrettyPrintingJson.encodeToString(serializer, value)).isEqualTo(json)
    assertThat(PrettyPrintingJson.decodeFromString(serializer, json)).isEqualTo(value)
  }
}
