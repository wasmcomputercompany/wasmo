package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.WitReader
import kotlin.test.Test

class WitKotlinGeneratorTest {
  @Test
  fun happyPath() {
    val witFile = WitReader(
      """
      |/// gets the epoch time
      |@since(version = 0.2.0)
      |interface wall-clock {
      |    /// A time and date in seconds plus nanoseconds.
      |    @since(version = 0.2.0)
      |    record datetime {
      |        seconds: u64,
      |        nanoseconds: u32,
      |    }
      |
      |    /// Read the current value of the clock.
      |    @since(version = 0.2.0)
      |    now: func() -> datetime;
      |}
      """.trimMargin(),
    ).read()

    val fileSpec = WitKotlinGenerator(
      kotlinPackageName = "com.example",
      witFile = witFile,
    ).generate()

    assertThat(fileSpec.toString()).isEqualTo(
      """
      |package com.example
      |
      |import kotlin.String
      |import kotlin.UInt
      |import kotlin.ULong
      |
      |/**
      | * gets the epoch time
      | */
      |public interface `wall-clock` {
      |  /**
      |   * Read the current value of the clock.
      |   */
      |  public fun now(): String
      |
      |  /**
      |   * A time and date in seconds plus nanoseconds.
      |   */
      |  public data class datetime(
      |    public val seconds: ULong,
      |    public val nanoseconds: UInt,
      |  )
      |}
      |
      """.trimMargin(),
    )
  }
}
