package com.wasmo.computers

import com.wasmo.api.InstallAppRequest
import com.wasmo.testing.WasmoArtifactServer
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8

class InstallAppActionTest {
  private lateinit var tester: WasmoServiceTester

  @BeforeTest
  fun setUp() {
    tester = WasmoServiceTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() = runTest {
    val computer = tester.createComputer("jesse124")
    val action = tester.installAppAction()
    val helloApp = WasmoArtifactServer.App(
      slug = "hello",
      displayName = "Hello World",
      wasm = "XXXX".encodeUtf8(),
    )
    tester.wasmoArtifactServer.apps += helloApp

    val installAppResponse = action.install(
      computerSlug = computer.slug,
      request = InstallAppRequest(
        manifestUrl = tester.baseUrl.resolve(helloApp.manifestPath)!!.toString(),
      ),
    )
  }
}
