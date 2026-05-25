package com.wasmo.postgresqloperator

/** Runs [block] with [name] as the thread name. */
internal fun <T> nameThread(name: String, block: () -> T): T {
  val oldName = Thread.currentThread().name
  Thread.currentThread().name = name
  try {
    return block()
  } finally {
    Thread.currentThread().name = oldName
  }
}
