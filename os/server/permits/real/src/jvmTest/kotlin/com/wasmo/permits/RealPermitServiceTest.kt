package com.wasmo.permits

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.PermitType
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.test.runTest
import wasmo.sql.SqlException

class RealPermitServiceTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun permitsReplenishedOnRollingWindow() = runTest {
    tester.clock.now += 8.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    tester.clock.now += 8.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    tester.clock.now += 8.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
    tester.clock.now += 8.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
    tester.clock.now += 8.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
  }

  @Test
  fun allPermitsReplenishedWhenDurationElapses() = runTest {
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)

    tester.clock.now += 23.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)

    tester.clock.now += 1.hours
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
  }

  @Test
  fun noCollisionFromSeparateValues() = runTest {
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)

    assertThat(tester.permitService.tryAcquire(Snack, "oreos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "oreos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "oreos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "oreos", ThreePerDay)).isEqualTo(false)
  }

  @Test
  fun noCollisionFromSeparateTypes() = runTest {
    assertThat(tester.permitService.tryAcquire(Snack, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "apple", ThreePerDay)).isEqualTo(false)

    assertThat(tester.permitService.tryAcquire(Computer, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Computer, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Computer, "apple", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Computer, "apple", ThreePerDay)).isEqualTo(false)
  }

  /**
   * Simulate a race by acquiring a permit after a decision has been made but before the database
   * is updated. This leverages a hook in the production code specifically created for this test!
   */
  @Test
  fun failsAfterRepeatedCollisions() = runTest {
    assertFailsWith<SqlException> {
      tester.permitService.tryAcquireWithHook(
        type = Snack,
        value = "doritos",
        rateLimit = ThreePerDay,
        hook = object : RealPermitService.Hook {
          override suspend fun beforeAcquire(serialNumber: Long) {
            tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)
          }
        },
      )
    }

    // No more permits, cause the collisions used them all up.
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
  }

  /** Like the test above, but only collide once. The code should recover gracefully. */
  @Test
  fun recoversFromSingleCollision() = runTest {
    var collisionCount = 0
    assertThat(
      tester.permitService.tryAcquireWithHook(
        type = Snack,
        value = "doritos",
        rateLimit = ThreePerDay,
        hook = object : RealPermitService.Hook {
          override suspend fun beforeAcquire(serialNumber: Long) {
            if (collisionCount++ == 0) {
              tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)
            }
          }
        },
      ),
    ).isEqualTo(true)

    // One permit left.
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(true)
    assertThat(tester.permitService.tryAcquire(Snack, "doritos", ThreePerDay)).isEqualTo(false)
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
