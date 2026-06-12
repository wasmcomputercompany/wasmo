package com.wasmo.sql

import com.wasmo.sql.PreservedStackTrace.Empty
import com.wasmo.sql.PreservedStackTrace.Full

/**
 * Preserves a stack trace for rethrowing later on, with the cause filled in.
 *
 * This is useful because, in production code, coroutines will discard stack frames once
 * they pass out of scope. So if you have this:
 *
 * ```kotlin
 * suspend fun myFunction() {
 *   createFuture().awaitSuspending()
 * }
 * ```
 *
 * ...by the future is running inside `awaitSuspending`, the stack frame that invoked `myFunction`
 * in the first place will be gone, since `awaitSuspending` had to suspend and dispatch to
 * get into a running state.
 *
 * This is why coroutines stack traces suck!
 *
 * You can fix this by preserving the stack trace before going and doing the suspendful thing:
 *
 * ```kotlin
 * suspend fun myFunction() {
 *   val preserves = PreserveStackTrace(shouldPreserve = true) {
 *     Exception("Something went wrong")
 *   }
 *   try {
 *     createFuture().awaitSuspending()
 *   } catch (e: Exception) {
 *     preserves.thawAndThrow(e)
 *   }
 * }
 *
 * ```
 *
 * This will preserve the stack trace at this one suspension point. Any suspensions further
 * up the stack trace can still throw away stack information.
 *
 * Saving this stack trace isn't free. (If it were, Kotlin would do it automatically all the time
 * instead of just under debug builds.) So putting this everywhere all the time is not great. 
 */
sealed interface PreservedStackTrace {
  fun thawAndThrow(cause: Throwable): Nothing

  object Empty : PreservedStackTrace {
    override fun thawAndThrow(cause: Throwable): Nothing {
      throw cause
    }
  }

  data class Full(val preservedStackTrace: Throwable) : PreservedStackTrace {
    override fun thawAndThrow(cause: Throwable): Nothing {
      preservedStackTrace.initCause(cause)

      val syntheticElement = StackTraceElement(javaClass.name, "thawAndThrow", "", 0)
      cause.stackTrace += arrayOf(syntheticElement) + preservedStackTrace.stackTrace
      throw cause
    }
  }
}

fun PreservedStackTrace(shouldPreserve: Boolean): PreservedStackTrace =
  if (shouldPreserve) {
    Full(Exception())
  } else {
    Empty
  }
