package com.wasmo.support.closetracker

import assertk.assertThat
import assertk.assertions.isEqualTo
import java.io.Closeable
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class CloseTrackerTest {
  @Test
  fun closedByCloseTrackerOnly() = runTest {
    val closeTracker = CloseTracker()
    val closeable = closeTracker.track { FakeCloseable(it) }
    closeTracker.closeAll()
    assertThat(closeable.closeCount).isEqualTo(1)
  }

  @Test
  fun closedByCloseableThenCloseTracker() = runTest {
    val closeTracker = CloseTracker()
    val closeable = closeTracker.track { FakeCloseable(it) }
    closeable.close()
    closeTracker.closeAll() // Does nothing.
    assertThat(closeable.closeCount).isEqualTo(1)
  }

  @Test
  fun closedByCloseTrackerAndThenCloseable() = runTest {
    val closeTracker = CloseTracker()
    val closeable = closeTracker.track { FakeCloseable(it) }
    closeTracker.closeAll()
    closeable.close()
    assertThat(closeable.closeCount).isEqualTo(2)
  }

  @Test
  fun everythingStillClosedIfAnyClosableThrows() = runTest {
    val closeTracker = CloseTracker()
    val closeable1 = closeTracker.track { FakeCloseable(it) }
    val closeable2 = closeTracker.track { FakeCloseable(it, Exception("boom!")) }
    val closeable3 = closeTracker.track { FakeCloseable(it) }
    assertFailsWith<Exception> {
      closeTracker.closeAll()
    }
    assertThat(closeable1.closeCount).isEqualTo(1)
    assertThat(closeable2.closeCount).isEqualTo(1)
    assertThat(closeable3.closeCount).isEqualTo(1)
  }

  class FakeCloseable(
    private val closeListener: CloseListener,
    private val throwOnClose: Throwable? = null,
  ) : Closeable {
    var closeCount = 0

    override fun close() {
      closeListener.onClose()
      closeCount++
      if (throwOnClose != null) throw throwOnClose
    }
  }
}
