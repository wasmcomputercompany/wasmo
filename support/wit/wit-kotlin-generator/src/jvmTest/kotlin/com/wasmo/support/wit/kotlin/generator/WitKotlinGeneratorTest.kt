package com.wasmo.support.wit.kotlin.generator

import assertk.assertThat
import assertk.assertions.containsExactly
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.WitPackage
import com.wasmo.support.wit.toWitFile
import kotlin.test.Test
import okio.Path.Companion.toPath

class WitKotlinGeneratorTest {
  @Test
  fun `full interface`() {
    val wasiClocks = WitPackage(
      packageName = PackageName("wasi", "clocks", "0.2.12"),
      files = mapOf(
        "clock.wit".toPath() to """
          |package wasi:clocks@0.2.12;
          |
          |/// gets the epoch time
          |@since(version = 0.2.0)
          |interface wall-clock {
          |    /// A time and date in seconds plus nanoseconds.
          |    @since(version = 0.2.0)
          |    record datetime {
          |        /// Since 1970.
          |        seconds: u64,
          |        /// Always less than 1000000000.
          |        nanoseconds: u32,
          |    }
          |
          |    /// Read the current value of the clock.
          |    @since(version = 0.2.0)
          |    now: func() -> datetime;
          |
          |    /// `pollable` represents a single I/O event which may be ready, or not.
          |    resource pollable {
          |        /// Return the readiness of a pollable. This function never blocks.
          |        ready: func() -> bool;
          |    }
          |
          |    /// Lookup error codes.
          |    variant error-code {
          |        /// Access denied.
          |        access-denied,
          |        /// A catch-all.
          |        other(option<string>),
          |    }
          |
          |    /// File or memory access pattern advisory information.
          |    enum advice {
          |        /// No advice.
          |        normal,
          |        /// sequentially from lower to higher.
          |        sequential,
          |    }
          |
          |    /// How paths are resolved.
          |    flags path-flags {
          |        /// Follow the darn things.
          |        symlink-follow,
          |    }
          |
          |    /// You can do both with an epoch.
          |    type instant = datetime;
          |}
          """.trimMargin().toWitFile(),
      ),
    )

    val fileSpecs = WitKotlinGenerator(
      witPackages = listOf(wasiClocks),
    ).generate()

    assertThat(fileSpecs.map { it.toString() }).containsExactly(
      """
      |package wit.wasi.clocks.v0_2_12
      |
      |import kotlin.Boolean
      |import kotlin.String
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
      |    /**
      |     * Since 1970.
      |     */
      |    public val seconds: ULong,
      |    /**
      |     * Always less than 1000000000.
      |     */
      |    public val nanoseconds: UInt,
      |  )
      |
      |  /**
      |   * `pollable` represents a single I/O event which may be ready, or not.
      |   */
      |  public interface Pollable {
      |    /**
      |     * Return the readiness of a pollable. This function never blocks.
      |     */
      |    public fun ready(): Boolean
      |  }
      |
      |  /**
      |   * Lookup error codes.
      |   */
      |  public sealed interface ErrorCode {
      |    /**
      |     * Access denied.
      |     */
      |    public data object AccessDenied : ErrorCode
      |
      |    /**
      |     * A catch-all.
      |     */
      |    public data class Other(
      |      public val `value`: String?,
      |    ) : ErrorCode
      |  }
      |
      |  /**
      |   * File or memory access pattern advisory information.
      |   */
      |  public sealed enum class Advice {
      |    /**
      |     * No advice.
      |     */
      |    Normal,
      |    /**
      |     * sequentially from lower to higher.
      |     */
      |    Sequential,
      |  }
      |
      |  /**
      |   * How paths are resolved.
      |   */
      |  public data class PathFlags(
      |    /**
      |     * Follow the darn things.
      |     */
      |    public val symlinkFollow: Boolean,
      |  )
      |}
      |
      """.trimMargin(),
    )
  }
}
