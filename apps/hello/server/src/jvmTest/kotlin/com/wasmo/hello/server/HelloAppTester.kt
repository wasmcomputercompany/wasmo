package com.wasmo.hello.server

import app.cash.burst.coroutines.CoroutineTestFunction
import app.cash.burst.coroutines.CoroutineTestInterceptor
import com.wasmo.FakeClock
import com.wasmo.common.tokens.newToken
import okio.Closeable
import okio.FileSystem
import okio.Path
import wasmo.app.WasmoApp

class HelloAppTester : CoroutineTestInterceptor {
  private var run: Run? = null

  override suspend fun intercept(testFunction: CoroutineTestFunction) {
    val fileSystem = FileSystem.SYSTEM
    val dataDirectory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "HelloAppTester" / newToken()
    fileSystem.createDirectories(dataDirectory)

    val run = Run(
      fileSystem = fileSystem,
      dataDirectory = dataDirectory,
    )

    run.use {
      run.app.afterInstall(
        oldVersion = 0L,
        newVersion = run.install.appVersion,
      )

      this.run = run
      try {
        testFunction()
      } finally {
        this.run = null
      }
    }
  }

  val clock: FakeClock
    get() = run!!.clock
  val install: WasmoApp.Install
    get() = run!!.install
  val app: HelloWasmoApp
    get() = run!!.app

  private class Run(
    val fileSystem: FileSystem,
    val dataDirectory: Path,
  ) : Closeable {
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

    override fun close() {
      app.close()
    }
  }
}
