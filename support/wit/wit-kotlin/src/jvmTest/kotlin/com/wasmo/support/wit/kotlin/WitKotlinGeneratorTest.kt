package com.wasmo.support.wit.kotlin

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.WitReader
import kotlin.test.Test

class WitKotlinGeneratorTest {
  @Test
  fun happyPath() {
    val witFile = WitReader(
      """
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
      """.trimMargin()
    ).read()

    val fileSpec = WitKotlinGenerator(witFile).generate()

    assertThat(fileSpec.toString()).isEqualTo(
      """
      |package com.example
      |
      |public interface `wall-clock`
      |
      """.trimMargin()
    )
  }
}
