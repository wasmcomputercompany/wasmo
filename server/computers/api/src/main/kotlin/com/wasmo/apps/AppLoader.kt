package com.wasmo.apps

import com.wasmo.api.AppManifest

interface AppLoader {
  suspend fun loadManifest(manifestUrl: String): AppManifest
  suspend fun downloadWasm(manifest: AppManifest)
}
