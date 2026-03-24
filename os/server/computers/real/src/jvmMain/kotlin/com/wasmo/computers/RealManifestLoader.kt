package com.wasmo.computers

import com.wasmo.framework.StateUserException
import com.wasmo.framework.checkUser
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.packaging.check
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.FileSystem
import okio.IOException
import wasmo.http.HttpRequest
import wasmo.http.HttpService

@Inject
@SingleIn(ComputerScope::class)
class RealManifestLoader(
  private val fileSystem: FileSystem,
  private val httpService: HttpService,
) : ManifestLoader {
  override suspend fun load(manifestAddress: ManifestAddress): AppManifest {
    val manifestString = when (manifestAddress) {
      is ManifestAddress.Http -> {
        val manifestResponse = try {
          httpService.execute(
            HttpRequest(
              method = "GET",
              url = manifestAddress.url,
            ),
          )
        } catch (e: IOException) {
          throw StateUserException("failed to fetch manifest", e)
        }

        checkUser(manifestResponse.isSuccessful) {
          "failed to fetch manifest: HTTP ${manifestResponse.code}"
        }

        manifestResponse.body.utf8()
      }

      is ManifestAddress.FileSystem -> {
        fileSystem.read(manifestAddress.path) {
          readUtf8()
        }
      }
    }

    val result = try {
      WasmoToml.decodeFromString(
        AppManifest.serializer(),
        manifestString,
      )
    } catch (e: Throwable) {
      throw StateUserException("failed to decode manifest\n\n${e.message}")
    }

    val issues = result.check()
    checkUser(issues.isEmpty()) {
      "invalid manifest\n\n${issues.joinToString(separator = "\n\n")}"
    }

    return result
  }
}
