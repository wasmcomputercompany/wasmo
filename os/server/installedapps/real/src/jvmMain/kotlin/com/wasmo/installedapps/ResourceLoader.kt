package com.wasmo.installedapps

import com.wasmo.packaging.AppManifest
import okio.ByteString

interface ResourceLoader {
  suspend fun loadManifest(): AppManifest
  suspend fun loadOrNull(resourcePath: String): ByteString?

  interface Factory {
    fun create(): ResourceLoader
  }
}
