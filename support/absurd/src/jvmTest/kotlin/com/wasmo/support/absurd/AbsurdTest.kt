@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
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
    val sandwichMaker = tester.sandwichMaker()
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

    tester.assertLogs(
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
  fun `self task is running`() = runTest {
    val taskName = TaskName<Unit, Unit>("task")
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = taskName,
        taskHandler = object : TaskHandler<Unit, Unit> {
          context(context: TaskHandler.Context)
          override suspend fun handle(params: Unit) {
            val result = tester.absurd().fetchTaskResult(context.taskId, context.taskName) ?: return
            tester.log("self task result is ${result::class.qualifiedName}")
          }
        },
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = taskName,
      params = Unit,
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isEqualTo(TaskResult.Completed(Unit))
    tester.assertLogs("self task result is ${TaskResult.Running::class.qualifiedName}")
  }

  @Test
  fun `cancel task before execution prevents all execution`() = runTest {
    val sandwichMaker = tester.sandwichMaker()
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

    absurd.cancelTask(spawnResult.taskId)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
    tester.assertLogs()

    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Cancelled())
  }

  @Test
  fun `cancel task after execution does nothing`() = runTest {
    val sandwichMaker = tester.sandwichMaker()
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

    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
      "taking bread: white",
      "taking toppings: [peanut butter, jam]",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isNotNull()
      .isInstanceOf<TaskResult.Completed<*, *>>()

    absurd.cancelTask(spawnResult.taskId)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isNotNull()
      .isInstanceOf<TaskResult.Completed<*, *>>()
  }

  @Test
  fun `cancel self then complete returns cancelled`() = runTest {
    val taskName = TaskName<Unit, Unit>("task")
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = taskName,
        taskHandler = object : TaskHandler<Unit, Unit> {
          context(context: TaskHandler.Context)
          override suspend fun handle(params: Unit) {
            tester.absurd().cancelTask(context.taskId)
          }
        },
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = taskName,
      params = Unit,
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isEqualTo(TaskResult.Cancelled())
  }

  @Test
  fun `cancel self then sleep returns cancelled`() = runTest {
    val taskName = TaskName<Unit, Unit>("task")
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = taskName,
        taskHandler = object : TaskHandler<Unit, Unit> {
          context(context: TaskHandler.Context)
          override suspend fun handle(params: Unit) {
            tester.absurd().cancelTask(context.taskId)
            tester.log("sleep starting...")
            context.sleepFor("after-cancel", 1.seconds)
            tester.log("sleep complete")
          }
        },
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = taskName,
      params = Unit,
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
      "sleep starting...",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isEqualTo(TaskResult.Cancelled())
  }

  @Test
  fun `cancel self then await event returns cancelled`() = runTest {
    val taskName = TaskName<Unit, Unit>("task")
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = taskName,
        taskHandler = object : TaskHandler<Unit, Unit> {
          context(context: TaskHandler.Context)
          override suspend fun handle(params: Unit) {
            tester.absurd().cancelTask(context.taskId)
            tester.log("await event starting...")
            context.awaitEvent<Unit>("any-event")
            tester.log("await event complete")
          }
        },
      ),
    )

    val spawnResult = absurd.spawn(
      taskName = taskName,
      params = Unit,
    )

    assertThat(spawnResult.attempt).isEqualTo(1)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
      "await event starting...",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, taskName))
      .isEqualTo(TaskResult.Cancelled())
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
    val sandwichMaker = tester.sandwichMaker()
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
    val sandwichMaker = tester.sandwichMaker()

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
    tester.assertLogs(
      "taking bread: white",
      "taking toppings: [peanut butter, jam]",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Pending())

    // Succeed the 2nd attempt.
    sandwichMaker.availableToppings.add("jam")
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
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
    val sandwichMaker = tester.sandwichMaker()

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
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
      "taking bread: white",
      "taking toppings: [peanut butter, jam]",
      "toasting for 30 seconds",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Sleeping())

    // Nothing to do immediately because the toast isn't ready.
    // TODO: Absurd's SQL doesn't honor the fake clock when selecting tasks, so this batch contains
    //   one more element than it needs to.
    tester.clock.sleep(29.seconds)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1) // Why not 0?
    tester.assertLogs(
      "toasting for 30 seconds",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Sleeping())

    // One more second and the toast is ready.
    tester.clock.sleep(1.seconds)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
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
  fun `cancel task while sleeping prevents resume`() = runTest {
    val sandwichMaker = tester.sandwichMaker()

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

    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(1)
    tester.assertLogs(
      "taking bread: white",
      "taking toppings: [peanut butter, jam]",
      "toasting for 30 seconds",
    )
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Sleeping())

    absurd.cancelTask(spawnResult.taskId)
    tester.clock.sleep(30.seconds)
    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
    tester.assertLogs()
    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isEqualTo(TaskResult.Cancelled())
  }

  @Test
  fun `fail without retries`() = runTest {
    val sandwichMaker = tester.sandwichMaker()

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
    tester.assertLogs(
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

