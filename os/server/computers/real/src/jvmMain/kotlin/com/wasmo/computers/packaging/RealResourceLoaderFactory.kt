package com.wasmo.computers.packaging

import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.ComputerScope
import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
internal class RealResourceLoaderFactory : ResourceLoader.Factory {
  override fun create(
    manifest: AppManifest,
    manifestAddress: AppManifestAddress,
  ): ResourceLoader {
    TODO("Not yet implemented")
  }
}
