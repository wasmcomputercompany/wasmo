package com.wasmo.computers

import com.wasmo.packaging.AppManifest

interface ManifestLoader {
  suspend fun load(appManifestAddress: AppManifestAddress): AppManifest
}
