@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.runTest

class AbsurdTest {
  @InterceptTest
  private val tester = AbsurdTester()

  @Test
  fun happyPath() = runTest {
    val sandwichMaker = SandwichMaker()
    tester.absurd.registerTask(
      name = SandwichMaker.TaskName,
      taskHandler = sandwichMaker,
    )

    val spawnResult = tester.absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(tester.absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)

    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )

    assertThat(tester.absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
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
  fun spawnUnregisteredTask() = runTest {
    val e = assertFailsWith<IllegalStateException> {
      tester.absurd.spawn(
        taskName = SandwichMaker.TaskName,
        params = MenuItem("PBJ"),
      )
    }
    assertThat(e).hasMessage("Task \"SandwichMaker\" is not registered.")
  }

  @Test
  fun spawnQueueDoesNotMatchRegisteredQueue() = runTest {
    val kitchenQueue = QueueName("kitchen")
    val sandwichMaker = SandwichMaker()
    tester.absurd.registerTask(
      name = SandwichMaker.TaskName,
      taskHandler = sandwichMaker,
    )
    val e = assertFailsWith<IllegalArgumentException> {
      tester.absurd.spawn(
        taskName = SandwichMaker.TaskName,
        params = MenuItem("PBJ"),
        queueName = kitchenQueue,
      )
    }
    assertThat(e).hasMessage(
      """Task "SandwichMaker" is registered for queue "default" """ +
        """but spawn requested queue "kitchen"""",
    )
  }

  @Test
  fun executeUnregisteredTask() = runTest {
    val sandwichMaker = SandwichMaker()
    tester.absurd.registerTask(
      name = SandwichMaker.TaskName,
      taskHandler = sandwichMaker,
    )
    tester.absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )

    val anotherAbsurd = Absurd(tester.postgresql)
    val e = assertFailsWith<IllegalStateException> {
      anotherAbsurd.executeBatch("sandwich-artist-1")
    }
    assertThat(e).hasMessage("task is not registered: SandwichMaker")
  }

  @Test
  fun failThenSucceed() = runTest {
    val sandwichMaker = SandwichMaker()

    tester.absurd.registerTask(
      name = SandwichMaker.TaskName,
      taskHandler = sandwichMaker,
    )

    val spawnResult = tester.absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )
    assertThat(spawnResult.attempt).isEqualTo(1)

    // Fail the first attempt.
    sandwichMaker.availableToppings.remove("jam")
    assertThat(tester.absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(tester.absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Pending())

    // Succeed the 2nd attempt.
    sandwichMaker.availableToppings.add("jam")
    assertThat(tester.absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(tester.absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
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
  fun failWithoutRetries() = runTest {
    val sandwichMaker = SandwichMaker()

    tester.absurd.registerTask(
      name = SandwichMaker.TaskName,
      taskHandler = sandwichMaker,
      defaultMaxAttempts = 1,
    )

    val spawnResult = tester.absurd.spawn(
      taskName = SandwichMaker.TaskName,
      params = MenuItem("PBJ"),
    )
    assertThat(spawnResult.attempt).isEqualTo(1)

    // Fail the first attempt.
    sandwichMaker.availableToppings.remove("jam")
    assertThat(tester.absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(sandwichMaker.log.receiveAvailable())
      .containsExactly(
        "taking bread: white",
        "taking toppings: [peanut butter, jam]",
      )
    assertThat(tester.absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isFailure(
        message = "no such topping: jam",
        throwableClass = IllegalStateException::class,
      )

    // Subsequent attempts do nothing.
    assertThat(tester.absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
  }
}

