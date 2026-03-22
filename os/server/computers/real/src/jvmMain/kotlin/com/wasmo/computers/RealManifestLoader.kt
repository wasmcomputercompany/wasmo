package com.wasmo.computers

import com.wasmo.framework.StateUserException
import com.wasmo.framework.checkUser
import com.wasmo.packaging.AppManifest
import com.wasmo.packaging.WasmoToml
import com.wasmo.packaging.check
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okhttp3.HttpUrl
import wasmo.http.HttpRequest
import wasmo.http.HttpService

@Inject
@SingleIn(ComputerScope::class)
class RealManifestLoader(
  private val httpService: HttpService,
) : ManifestLoader {
  override suspend fun loadManifest(manifestUrl: HttpUrl): AppManifest {
    val manifestResponse = httpService.execute(
      HttpRequest(
        method = "GET",
        url = manifestUrl,
      ),
    )

    checkUser(manifestResponse.isSuccessful) {
      "failed to fetch manifest: HTTP ${manifestResponse.code}"
    }

    val result = try {
      WasmoToml.decodeFromString(
        AppManifest.serializer(),
        manifestResponse.body.utf8(),
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
