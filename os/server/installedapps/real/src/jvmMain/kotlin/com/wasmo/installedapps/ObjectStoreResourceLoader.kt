package com.wasmo.installedapps

import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import okio.ByteString
import wasmo.objectstore.GetObjectRequest
import wasmo.objectstore.ObjectStore

@Inject
@SingleIn(InstalledAppScope::class)
class ObjectStoreResourceLoader(
  private val appManifest: AppManifest,
  @ForInstalledApp private val objectStore: ObjectStore,
) : ResourceLoader {
  override suspend fun loadManifest() = appManifest

  override suspend fun loadOrNull(resourcePath: String): ByteString? {
    check(resourcePath.startsWith("/"))

    val getObjectResponse = objectStore.get(
      request = GetObjectRequest(
        key = "resources/v${appManifest.version}$resourcePath",
      ),
    )

    val responseBody = getObjectResponse.value
      ?: return null

    return responseBody
  }
}
