package com.wasmo.computers

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ForComputer
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.issues.IssueCollector
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.AppManifestChecker
import com.wasmo.packaging.WasmoToml
import dev.eav.tomlkt.decodeFromNativeReader
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlinx.serialization.SerializationException
import okio.Buffer
import okio.ByteString
import okio.IOException
import okio.buffer
import okio.source
import wasmo.http.HttpRequest
import wasmo.http.HttpService
import wasmo.objectstore.ObjectStore
import wasmo.objectstore.PutObjectRequest
import wasmo.objectstore.ScopedObjectStore

/**
 * Downloads a `.wasmo` ZIP file from the Internet and writes it to the object store.
 */
@AssistedInject
class ZipResourceInstaller(
  @ForComputer private val computerObjectStore: ObjectStore,
  private val httpService: HttpService,
  @Assisted private val appSlug: AppSlug,
  @Assisted private val wasmoFileAddress: WasmoFileAddress.Http,
) : ResourceInstaller {
  context(issueCollector: IssueCollector)
  override suspend fun install(): AppManifest? {
    context(issueCollector.url(wasmoFileAddress.url.toString())) {
      return installInternal()
    }
  }

  context(issueCollector: IssueCollector)
  private suspend fun installInternal(): AppManifest? {
    val httpResponse = try {
      httpService.execute(
        HttpRequest(
          url = wasmoFileAddress.url.toString(),
        ),
      )
    } catch (e: IOException) {
      issueCollector.add("HTTP request failed", e)
      return null
    }

    if (!httpResponse.isSuccessful) {
      issueCollector.add("HTTP request failed: ${httpResponse.code}")
      return null
    }

    val zip = httpResponse.body
    val appManifest = loadManifest(zip = zip)
      ?: return null

    context(issueCollector.path("wasmo-manifest.toml")) {
      AppManifestChecker().check(appManifest)
    }
    if (issueCollector.hasFatalIssues) return null

    copyZipEntriesToObjectStore(
      appManifest = appManifest,
      zip = zip,
    )
    if (issueCollector.hasFatalIssues) return null

    return appManifest
  }

  context(issueCollector: IssueCollector)
  fun loadManifest(zip: ByteString): AppManifest? {
    try {
      zip.zipInputStream().use { zipInputStream ->
        val zipEntry = zipInputStream.entries()
          .firstOrNull { it.name == "wasmo-manifest.toml" }
        if (zipEntry == null) {
          issueCollector.add("No wasmo-manifest.toml file in .wasmo archive")
          return null
        }

        try {
          return WasmoToml.decodeFromNativeReader<AppManifest>(
            nativeReader = InputStreamReader(zipInputStream, StandardCharsets.UTF_8),
          )
        } catch (e: SerializationException) {
          issueCollector.path("wasmo-manifest.toml").add("Reading manifest failed", e)
          return null
        }
      }
    } catch (e: IOException) {
      issueCollector.add("Reading .wasmo archive failed", e)
      return null
    }
  }

  context(issueCollector: IssueCollector)
  suspend fun copyZipEntriesToObjectStore(
    appManifest: AppManifest,
    zip: ByteString,
  ) {
    val resourcesObjectStore = ScopedObjectStore(
      delegate = computerObjectStore,
      prefix = "$appSlug/resources/v${appManifest.version}/",
    )

    try {
      zip.zipInputStream().use { zipInputStream ->
        for (zipEntry in zipInputStream.entries()) {
          val zipEntryName = zipEntry.name
          context(issueCollector.path(zipEntryName)) {
            copyZipEntryToObjectStore(zipInputStream, resourcesObjectStore, zipEntryName)
            if (issueCollector.hasFatalIssues) return
          }
        }
      }
    } catch (e: IOException) {
      issueCollector.add("Reading .wasmo archive failed", e)
    }
  }

  context(issueCollector: IssueCollector)
  private suspend fun copyZipEntryToObjectStore(
    inputStream: InputStream,
    objectStore: ScopedObjectStore,
    key: String,
  ) {
    val value = try {
      inputStream.source().buffer().readByteString()
    } catch (e: IOException) {
      issueCollector.add("Reading resource from archive failed", e)
      return
    }

    try {
      objectStore.put(
        PutObjectRequest(
          key = key,
          value = value,
        ),
      )
    } catch (e: IOException) {
      issueCollector.add("Storing resource failed", e)
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(
      appSlug: AppSlug,
      wasmoFileAddress: WasmoFileAddress.Http,
    ): ZipResourceInstaller
  }
}

private fun ZipInputStream.entries(): Sequence<ZipEntry> {
  return sequence {
    while (true) {
      val entry = nextEntry ?: break
      yield(entry)
    }
  }
}

private fun ByteString.zipInputStream() = ZipInputStream(Buffer().write(this).inputStream())
