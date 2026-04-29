package com.wasmo.permits

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.PermitType
import com.wasmo.permits.PermitService.Hook
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import wasmo.sql.SqlException
import wasmox.sql.transaction

class RealPermitServiceTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun `permits replenished on rolling window`() = runTest {
    tester.clock.now += 8.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    tester.clock.now += 8.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    tester.clock.now += 8.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
    tester.clock.now += 8.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
    tester.clock.now += 8.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  @Test
  fun `all permits replenished when duration elapses`() = runTest {
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)

    tester.clock.now += 23.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)

    tester.clock.now += 1.hours
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  @Test
  fun `permit replenished by release`() = runTest {
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)

    assertThat(tryAcquire(Snack, "doritos", count = -1L)).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  @Test
  fun `proactive release is possible`() = runTest {
    assertThat(tryAcquire(Snack, "doritos", count = -1L)).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  @Test
  fun `no collision from separate values`() = runTest {
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)

    assertThat(tryAcquire(Snack, "oreos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "oreos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "oreos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "oreos")).isEqualTo(false)
  }

  @Test
  fun `no collision from separate types`() = runTest {
    assertThat(tryAcquire(Snack, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "apple")).isEqualTo(false)

    assertThat(tryAcquire(Computer, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Computer, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Computer, "apple")).isEqualTo(true)
    assertThat(tryAcquire(Computer, "apple")).isEqualTo(false)
  }

  /**
   * Simulate a race by acquiring a permit after a decision has been made but before the database
   * is updated. This leverages a hook in the production code specifically created for this test!
   */
  @Test
  fun `fails after repeated collisions`() = runTest {
    assertFailsWith<SqlException> {
      tryAcquire(
        type = Snack,
        value = "doritos",
        rateLimit = ThreePerDay,
        hook = object : Hook {
          override suspend fun beforeAcquire(serialNumber: Long) {
            tryAcquire(Snack, "doritos")
          }
        },
      )
    }

    // No more permits, cause the collisions used them all up.
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  /** Like the test above, but only collide once. The code should recover gracefully. */
  @Test
  fun recoversFromSingleCollision() = runTest {
    var collisionCount = 0
    assertThat(
      tryAcquire(
        type = Snack,
        value = "doritos",
        rateLimit = ThreePerDay,
        hook = object : Hook {
          override suspend fun beforeAcquire(serialNumber: Long) {
            if (collisionCount++ == 0) {
              tryAcquire(Snack, "doritos")
            }
          }
        },
      ),
    ).isEqualTo(true)

    // One permit left.
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(true)
    assertThat(tryAcquire(Snack, "doritos")).isEqualTo(false)
  }

  private suspend fun tryAcquire(
    type: PermitType,
    value: String,
    count: Long = 1L,
    rateLimit: RateLimit = ThreePerDay,
    hook: Hook? = null,
  ): Boolean {
    val now = tester.clock.now()
    return tester.wasmoDb.transaction(attemptCount = 3) {
      tester.permitService.tryAcquire(
        now = now,
        type = type,
        value = value,
        count = count,
        rateLimit = rateLimit,
        hook = hook,
      )
    }
  }

  companion object {
    val Snack = PermitType("Snack")
    val Computer = PermitType("Computer")
    val ThreePerDay = RateLimit(
      count = 3,
      duration = 24.hours,
    )
  }
}
