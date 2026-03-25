package com.wasmo.computers.packaging

import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.packaging.AppManifest
import okio.ByteString

interface ResourceLoader {
  suspend fun loadManifest(): AppManifest
  suspend fun load(resourcePath: String): ByteString?

  interface Factory {
    fun create(
      manifest: AppManifest,
      manifestAddress: AppManifestAddress,
    ): ResourceLoader
  }
}

