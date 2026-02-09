package com.wasmo.admin.server

import com.wasmo.FakeClock
import com.wasmo.FakeHttpClient
import com.wasmo.WasmoApp
import com.wasmo.admin.api.AdminJson
import com.wasmo.common.tokens.newToken
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path

class AdminAppTester private constructor(
  val fileSystem: FileSystem,
  val dataDirectory: Path,
) {
  val clock = FakeClock()
  val baseUrl = "https://example.com/".toHttpUrl()
  val wasmoArtifactServer = WasmoArtifactServer(AdminJson)
  val httpClient = FakeHttpClient().apply {
    this += wasmoArtifactServer
  }
  val install = WasmoApp.Install(
    appVersion = 1L,
    dataDirectory = dataDirectory,
    fileSystem = fileSystem,
  )

  val app = AdminWasmoApp(
    clock = clock,
    httpClient = httpClient,
    install = install,
  )

  fun close() {
    app.close()
  }

  companion object {
    fun start(): AdminAppTester {
      val fileSystem = FileSystem.SYSTEM
      val dataDirectory = FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "AdminAppTester" / newToken()
      fileSystem.createDirectories(dataDirectory)

      val tester = AdminAppTester(
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
