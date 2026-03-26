package com.wasmo.computers.packaging

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.Issue
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.testing.buildZip
import kotlin.random.Random
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.ByteString.Companion.toByteString
import okio.IOException
import wasmo.http.FakeHttpService
import wasmo.objectstore.FakeObjectStore

class ZipResourceInstallerTest {
  private val url = "https://example.com/hello.wasmo".toHttpUrl()
  private val objectStore = FakeObjectStore()
  private val wasmoFileAddress = WasmoFileAddress.Http(url)
  private val httpService = FakeHttpService()
  private val installer = ZipResourceInstaller(
    computerObjectStore = objectStore,
    wasmoFileAddress = wasmoFileAddress,
    httpService = httpService,
    appSlug = AppSlug("music"),
  )

  @Test
  fun happyPath() = runTest {
    val manifest = AppManifest(
      version = 1L,
      target = TargetSdk1,
    )

    val wasmoFile = buildZip {
      put(manifest)
      put("assets/index.html", "<title>Music!</title>".encodeUtf8())
    }
    httpService[url] = wasmoFile

    val issueCollector = IssueCollector()
    val installedManifest = with(issueCollector) {
      installer.install()
    }

    assertThat(installedManifest).isEqualTo(manifest)
    assertThat(issueCollector.issues).isEmpty()
    assertThat(objectStore["music/resources/v1/wasmo-manifest.toml"])
      .isEqualTo(WasmoToml.encodeToString(manifest).encodeUtf8())
    assertThat(objectStore["music/resources/v1/assets/index.html"])
      .isEqualTo("<title>Music!</title>".encodeUtf8())
  }

  @Test
  fun emptyZip() = runTest {
    val wasmoFile = buildZip {
    }
    httpService[url] = wasmoFile

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues).containsExactly(
      Issue(
        message = "No wasmo-manifest.toml file in .wasmo archive",
        url = url.toString(),
      ),
    )
  }

  @Test
  fun zipHttpNotFound() = runTest {
    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues).containsExactly(
      Issue(
        message = "HTTP request failed: 404",
        url = url.toString(),
      ),
    )
  }

  @Test
  fun zipHttpError() = runTest {
    httpService += FakeHttpService.Handler {
      throw IOException("boom!")
    }

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "HTTP request failed",
        url = url.toString(),
      ),
    )
  }

  @Test
  fun malformedManifest() = runTest {
    val wasmoFile = buildZip {
      put("wasmo-manifest.toml", "{}".encodeUtf8())
    }
    httpService[url] = wasmoFile

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Reading manifest failed",
        url = url.toString(),
        path = "wasmo-manifest.toml",
      ),
    )
  }

  @Test
  fun invalidManifest() = runTest {
    val manifest = AppManifest(
      version = -1L,
      target = "https://wasmo.com/sdk/foo",
    )

    val wasmoFile = buildZip {
      put(manifest)
    }
    httpService[url] = wasmoFile

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
        url = url.toString(),
        path = "wasmo-manifest.toml",
        href = "target",
      ),
      Issue(
        message = """
          |unexpected version -1
          |expected a positive integer
          """.trimMargin(),
        url = url.toString(),
        path = "wasmo-manifest.toml",
        href = "version",
      ),
    )
  }

  /** Truncate the .zip archive and confirm that triggers a failure. */
  @Test
  fun invalidZipArchive() = runTest {
    val manifest = AppManifest(
      version = 1L,
      target = TargetSdk1,
    )

    val sampleResource = run {
      val data = ByteArray(1024 * 1024)
      Random.nextBytes(data)
      data.toByteString()
    }

    val originalZip = buildZip {
      put(manifest)
      put("file.txt", sampleResource)
    }

    httpService[url] = run {
      val buffer = Buffer()
      buffer.write(originalZip)
      buffer.readByteString(buffer.size - 1024L)
    }

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Reading resource from archive failed",
        url = url.toString(),
        path = "file.txt",
      ),
    )
  }

  @Test
  fun objectStoreRejectsWrite() = runTest {
    val manifest = AppManifest(
      version = 1L,
      target = TargetSdk1,
    )

    httpService[url] = buildZip {
      put(manifest)
    }

    objectStore.nextException = IOException("boom!")

    val issueCollector = IssueCollector()
    with(issueCollector) {
      installer.install()
    }

    assertThat(issueCollector.issues.stripExceptions()).containsExactly(
      Issue(
        message = "Storing resource failed",
        path = "wasmo-manifest.toml",
        url = url.toString(),
      ),
    )
  }

  /** Removes the exceptions from the issues for better assertions. */
  private fun List<Issue>.stripExceptions(): List<Issue> = map { it.copy(exception = null) }
}
