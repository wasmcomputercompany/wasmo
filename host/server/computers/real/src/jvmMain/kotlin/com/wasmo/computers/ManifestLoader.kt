package com.wasmo.computers

import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl

interface ManifestLoader {
  suspend fun loadManifest(manifestUrl: HttpUrl): AppManifest
}
