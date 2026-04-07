package com.wasmo.sql.r2dbc

import java.io.Closeable
import java.util.concurrent.LinkedBlockingDeque

interface CloseListener {
  fun onClose()
}

/**
 * Keeps track closeable things so we can close them when the enclosing service is closed.
 *
 * We could make this more capable by using something like LeakCanary.
 */
class CloseTracker {
  private val entries = LinkedBlockingDeque<Entry>()

  suspend fun <T : Closeable> track(block: suspend (CloseListener) -> T): T {
    val entry = Entry()
    val result = block(entry)
    entry.closeable = result
    entries += entry
    return result
  }

  fun closeAll() {
    var thrown: Throwable? = null

    val i = entries.iterator()
    while (i.hasNext()) {
      val handle = i.next()
      i.remove()
      try {
        handle.closeable.close()
      } catch (e: Throwable) {
        if (thrown == null) {
          thrown = e
        } else {
          thrown.addSuppressed(e)
        }
      }
    }

    if (thrown != null) throw thrown
  }

  private inner class Entry : CloseListener {
    lateinit var closeable: Closeable

    override fun onClose() {
      entries.remove(this)
    }
  }
}
