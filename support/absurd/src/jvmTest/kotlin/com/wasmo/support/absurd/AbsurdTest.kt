@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.runTest

class AbsurdTest {
  @InterceptTest
  private val tester = AbsurdTester()

  @Test
  fun `happy path`() = runTest {
    val sandwichMaker = SandwichMaker()
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)

    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )

    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(
        TaskResult.Completed(
          Sandwich(
            bread = "white",
            toppings = listOf("peanut butter", "jam"),
            toasted = false,
          ),
        ),
      )
  }

  @Test
  fun `spawn unregistered task`() = runTest {
    val absurd = tester.absurd()
    val e = assertFailsWith<IllegalStateException> {
      absurd.spawn(
        taskName = SandwichMaker.TaskName,
        params = MenuItem("PBJ"),
      )
    }
    assertThat(e).hasMessage("Task \"SandwichMaker\" is not registered.")
  }

  @Test
  fun `execute unregistered task`() = runTest {
    val sandwichMaker = SandwichMaker()
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
      ),
    )
    absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )

    val anotherAbsurd = tester.absurd()
    val e = assertFailsWith<IllegalStateException> {
      anotherAbsurd.executeBatch("sandwich-artist-1")
    }
    assertThat(e).hasMessage("task is not registered: SandwichMaker")
  }

  @Test
  fun `fail then succeed`() = runTest {
    val sandwichMaker = SandwichMaker()

    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )
    assertThat(spawnResult.attempt).isEqualTo(1)

    // Fail the first attempt.
    sandwichMaker.availableToppings.remove("jam")
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Pending())

    // Succeed the 2nd attempt.
    sandwichMaker.availableToppings.add("jam")
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(
        TaskResult.Completed(
          Sandwich(
            bread = "white",
            toppings = listOf("peanut butter", "jam"),
            toasted = false,
          ),
        ),
      )
  }

  @Test
  fun `task sleeps`() = runTest {
    val sandwichMaker = SandwichMaker()

    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("toasted PBJ"),
    )
    assertThat(spawnResult.attempt).isEqualTo(1)

    // Succeed until the toasting step.
    sandwichMaker.availableToppings.add("jam")
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
        "toasting for 30 seconds",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Pending())

    // Nothing to do immediately because the toast isn't ready.
    // TODO: Absurd's SQL doesn't honor the fake clock when selecting tasks, so this batch contains
    //   one more element than it needs to.
    tester.clock.sleep(29.seconds)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1) // Why not 0?
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "toasting for 30 seconds",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Pending())

    // One more second and the toast is ready.
    tester.clock.sleep(1.seconds)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "toasting for 30 seconds",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(
        TaskResult.Completed(
          Sandwich(
            bread = "white",
            toppings = listOf("peanut butter", "jam"),
            toasted = true,
          ),
        ),
      )
  }

  @Test
  fun `fail without retries`() = runTest {
    val sandwichMaker = SandwichMaker()

    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
        defaultMaxAttempts = 1,
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )
    assertThat(spawnResult.attempt).isEqualTo(1)

    // Fail the first attempt.
    sandwichMaker.availableToppings.remove("jam")
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isFailure(
        message = "no such topping: jam",
        throwableClass = IllegalStateException::class,
      )

    // Subsequent attempts do nothing.
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
  }
}

