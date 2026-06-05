package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.toWitFile
import kotlin.test.Test
import okio.Path.Companion.toPath

class WitKotlinGeneratorTest {
  @Test
  fun happyPath() {
    val witFile = """
      |package wasi:clocks@0.2.12;
      |
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
      """.trimMargin().toWitFile()

    val witPackages = listOf(
      WitPackage(
        packageName = PackageName("wasi", "clocks", "0.2.12"),
        files = mapOf("clock.wit".toPath() to witFile)
      ),
    )
    val fileSpecs = WitKotlinGenerator(
      witPackages = witPackages,
    ).generate()

    assertThat(fileSpecs.single().toString()).isEqualTo(
      """
      |package wit.wasi.clocks.v0_2_12
      |
      |import kotlin.UInt
      |import kotlin.ULong
      |
      |/**
      | * gets the epoch time
      | */
      |public interface WallClock {
      |  /**
      |   * Read the current value of the clock.
      |   */
      |  public fun now(): Datetime
      |
      |  /**
      |   * A time and date in seconds plus nanoseconds.
      |   */
      |  public data class Datetime(
      |    public val seconds: ULong,
      |    public val nanoseconds: UInt,
      |  )
      |}
      |
      """.trimMargin(),
    )
  }
}
