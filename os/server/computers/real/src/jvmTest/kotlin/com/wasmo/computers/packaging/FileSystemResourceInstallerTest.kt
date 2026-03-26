package com.wasmo.computers.packaging

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.Issue
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import wasmo.objectstore.FakeObjectStore

class FileSystemResourceInstallerTest {
  private val objectStore = FakeObjectStore()
  private val fileSystem = FakeFileSystem()
  private val wasmoFileAddress = WasmoFileAddress.FileSystem(
    "/Development/music/music.wasmo".toPath(),
  )
  private val installer = FileSystemResourceInstaller(
    wasmoFileAddress = wasmoFileAddress,
    fileSystem = fileSystem,
  )

  @Test
  fun happyPath() = runTest {
    val manifest = AppManifest(
      version = 1L,
      target = TargetSdk1,
    )

    fileSystem.createDirectories(wasmoFileAddress.path)
    fileSystem.write(wasmoFileAddress.path / "wasmo-manifest.toml") {
      writeUtf8(WasmoToml.encodeToString<AppManifest>(manifest))
    }
    fileSystem.createDirectories(wasmoFileAddress.path / "assets")
    fileSystem.write(wasmoFileAddress.path / "assets" / "index.html") {
      writeUtf8("<title>Music!</title>")
    }

    val issueCollector = IssueCollector()
    val installedManifest = with(issueCollector) {
      installer.install()
    }

    assertThat(installedManifest).isEqualTo(manifest)
    assertThat(issueCollector.issues).isEmpty()
    assertThat(objectStore.list("music/resources/v1/wasmo-manifest.toml")).isEmpty()
  }

  @Test
  fun emptyDirectory() = runTest {
    fileSystem.createDirectories(wasmoFileAddress.path)

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Manifest file not found",
        path = (wasmoFileAddress.path / "wasmo-manifest.toml").toString(),
      ),
    )
  }

  @Test
  fun directoryNotFound() = runTest {
    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Not a directory",
        path = wasmoFileAddress.path.toString(),
      ),
    )
  }

  @Test
  fun malformedManifest() = runTest {
    fileSystem.createDirectories(wasmoFileAddress.path)
    fileSystem.write(wasmoFileAddress.path / "wasmo-manifest.toml") {
      writeUtf8("{}")
    }

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Decoding manifest failed",
        path = (wasmoFileAddress.path / "wasmo-manifest.toml").toString(),
      ),
    )
  }

  @Test
  fun invalidManifest() = runTest {
    val manifest = AppManifest(
      version = -1L,
      target = "https://wasmo.com/sdk/foo",
    )

    fileSystem.createDirectories(wasmoFileAddress.path)
    fileSystem.write(wasmoFileAddress.path / "wasmo-manifest.toml") {
      writeUtf8(WasmoToml.encodeToString<AppManifest>(manifest))
    }

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues).containsExactly(
      Issue(
        message = """
          |unsupported target 'https://wasmo.com/sdk/foo'
          |expected one of [https://wasmo.com/sdk/1]
          """.trimMargin(),
        path = (wasmoFileAddress.path / "wasmo-manifest.toml").toString(),
        href = "target",
      ),
      Issue(
        message = """
          |unexpected version -1
          |expected a positive integer
          """.trimMargin(),
        path = (wasmoFileAddress.path / "wasmo-manifest.toml").toString(),
        href = "version",
      ),
    )
  }

  /** Removes the exceptions from the issues for better assertions. */
  private fun List<Issue>.stripExceptions(): List<Issue> = map { it.copy(exception = null) }
}
