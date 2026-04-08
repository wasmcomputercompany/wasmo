package com.wasmo.installedapps

import okio.ByteString

interface ResourceLoader {
  suspend fun loadOrNull(resourcePath: String): ByteString?

  interface Factory {
    fun create(): ResourceLoader
  }
}
