package com.wasmo.installedapps

import com.wasmo.packaging.AppManifest

interface AppManifestLoader {
  suspend fun load(): AppManifest
}
