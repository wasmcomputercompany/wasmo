package com.wasmo.computers.packaging

import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.packaging.AppManifest
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
class RealResourceLoaderFactory : ResourceLoader.Factory {
  override fun create(
    manifest: AppManifest,
    wasmoFileAddress: WasmoFileAddress,
  ): ResourceLoader {
    TODO("Not yet implemented")
  }
}
