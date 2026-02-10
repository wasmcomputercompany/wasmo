package com.wasmo.hello.server

import com.wasmo.FakeClock
import com.wasmo.WasmoApp
import com.wasmo.common.tokens.newToken
import okio.FileSystem
import okio.Path

class HelloAppTester private constructor(
  val fileSystem: FileSystem,
  val dataDirectory: Path,
) {
  val clock = FakeClock()
  val install = WasmoApp.Install(
    appVersion = 1L,
    dataDirectory = dataDirectory,
    fileSystem = fileSystem,
  )

  val app = HelloWasmoApp(
    clock = clock,
    install = install,
  )

  fun close() {
    app.close()
  }

  companion object {
    fun start(): HelloAppTester {
      val fileSystem = FileSystem.SYSTEM
      val dataDirectory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "HelloAppTester" / newToken()
      fileSystem.createDirectories(dataDirectory)

      val tester = HelloAppTester(
        fileSystem = fileSystem,
        dataDirectory = dataDirectory,
      )

      tester.app.afterInstall(
        oldVersion = 0L,
        newVersion = tester.install.appVersion,
      )

      return tester
    }
  }
}
