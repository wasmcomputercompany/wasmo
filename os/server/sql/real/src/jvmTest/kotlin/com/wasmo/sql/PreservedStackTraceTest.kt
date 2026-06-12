package com.wasmo.sql

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield

/**
 * This test starts with a reasonable enough stacktrace:
 *
 * ```
 * Exception("boom")
 *   at PreservedStackTraceTest.stackFrame0()
 *   at PreservedStackTraceTest.stackFrame1()
 *   at PreservedStackTraceTest.stackFrame2()
 *   at PreservedStackTraceTest.stackFrame3()
 *   at PreservedStackTraceTest.stackFrame4()
 *   at PreservedStackTraceTest.happyPath()
 * ```
 *
 * But then it trashes the stack trace at `stackFrame2` by suspending. Normally this would not have
 * `stackFrame3()` and `stackFrame4()`, which are critical to understanding what went wrong:
 *
 * ```
 * java.lang.Exception: boom!
 * 	at PreservedStackTraceTest.stackFrame0()
 * 	at PreservedStackTraceTest.stackFrame1()
 * 	at PreservedStackTraceTest.stackFrame2()
 * 	at PreservedStackTraceTest$stackFrame2$1.invokeSuspend()
 * 	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith()
 * 	at kotlinx.coroutines.DispatchedTask.run()
 * 	...
 * ```
 *
 * But [preserveStackTrace] does its job, and we get those frames back.
 */
class PreservedStackTraceTest {
  @Test
  fun happyPath() = runTest {
    val exception = assertFailsWith<Exception> {
      stackFrame4()
    }

    exception.printStackTrace()

    assertThat(exception.stackTrace.take(7).map { it.methodName }).containsExactly(
      "stackFrame0",
      "stackFrame1",
      "stackFrame2",
      "invokeSuspend", // This is the yield.
      "stackFrame2",
      "stackFrame3",
      "stackFrame4",
    )
  }

  suspend fun stackFrame4() = stackFrame3()
  suspend fun stackFrame3() = stackFrame2()
  suspend fun stackFrame2() {
    preserveStackTrace {
      yield() // Trash the stack.
      stackFrame1()
    }
  }

  suspend fun stackFrame1() = stackFrame0()
  suspend fun stackFrame0() {
    throw Exception("boom!")
  }
}
