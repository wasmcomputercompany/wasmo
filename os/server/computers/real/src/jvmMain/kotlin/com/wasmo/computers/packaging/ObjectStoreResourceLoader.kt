package com.wasmo.computers.packaging

import com.wasmo.packaging.AppManifest
import okio.ByteString

internal class ObjectStoreResourceLoader: ResourceLoader {
  override suspend fun loadManifest(): AppManifest {
    TODO("Not yet implemented")
  }

  override suspend fun load(resourcePath: String): ByteString? {
    TODO("Not yet implemented")
  }
}
