package com.wasmo.computers.packaging

import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.ComputerScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
internal class RealInstallerFactory : Installer.Factory {
  override fun create(manifestAddress: AppManifestAddress): Installer {
    TODO("Not yet implemented")
  }
}


