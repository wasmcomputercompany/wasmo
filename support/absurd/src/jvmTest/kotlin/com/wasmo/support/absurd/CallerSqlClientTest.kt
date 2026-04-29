@file:OptIn(ExperimentalUuidApi::class)

package com.wasmo.support.absurd

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.test.runTest

class CallerSqlClientTest {
  @InterceptTest
  private val tester = AbsurdTester()

  @Test
  fun `roll back spawn`() = runTest {
    val sandwichMaker = tester.sandwichMaker()
    val absurd = tester.absurd(
      TaskRegistration(
        taskName = SandwichMaker.TaskName,
        taskHandler = sandwichMaker,
      ),
    )

    val spawnResult = tester.postgresql.withConnection {
      begin()

      val spawnResult = absurd.spawn(
        taskName = SandwichMaker.TaskName,
        params = MenuItem("PBJ"),
        sqlClient = this,
      )

      assertThat(
        absurd.fetchTaskResult(
          taskId = spawnResult.taskId,
          taskName = SandwichMaker.TaskName,
          sqlClient = this,
        ),
      ).isEqualTo(TaskResult.Pending())

      rollback()
      return@withConnection spawnResult
    }

    assertThat(spawnResult.attempt).isEqualTo(1)

    assertThat(absurd.executeBatch("sandwich-artist-1")).isEqualTo(0)
    tester.assertLogs()

    assertThat(absurd.fetchTaskResult(spawnResult.taskId, SandwichMaker.TaskName))
      .isNull()
  }

  @Test
  fun `roll back cancel`() = runTest {
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

    tester.postgresql.withConnection {
      begin()

      absurd.cancelTask(
        taskId = spawnResult.taskId,
        sqlClient = this,
      )

      rollback()
    }

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
}
