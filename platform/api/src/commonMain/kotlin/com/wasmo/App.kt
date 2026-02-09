package com.wasmo

import okio.FileSystem
import okio.Path

interface WasmoApp {
  /**
   * Invoked after an app is first installed, and after each version update.
   *
   * @param oldVersion will be 0 if this is the first install of the app.
   */
  fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  )

  data class Install(
    val appVersion: Long,
    val dataDirectory: Path,
    val fileSystem: FileSystem,
  )
}
