package com.wasmo.computers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.InstallAppRequest
import com.wasmo.testing.WasmoArtifactServer
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath

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
    val client = tester.newClient()
    val computer = client.createComputer("jesse124")
    val action = client.installAppAction()
    val wasm = "XXXX".encodeUtf8()
    val helloApp = WasmoArtifactServer.App(
      slug = "hello",
      displayName = "Hello World",
      version = 1L,
      wasm = wasm,
    )
    tester.wasmoArtifactServer.apps += helloApp

    val installAppResponse = action.install(
      computerSlug = computer.slug,
      request = InstallAppRequest(
        manifestUrl = tester.deployment.baseUrl.resolve(helloApp.manifestPath)!!.toString(),
      ),
    )

    assertThat(
      tester.fileSystem.read("/jesse124/apps/hello/v1/app.wasm".toPath()) {
        readByteString()
      },
    ).isEqualTo(wasm)
  }
}
