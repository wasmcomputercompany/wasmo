package com.wasmo.computers.packaging

import com.wasmo.identifiers.WasmoFileAddress
import com.wasmo.identifiers.ComputerScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
class RealInstallerFactory(
  private val zipInstallerFactory: ZipInstaller.Factory,
) : Installer.Factory {
  override fun create(wasmoFileAddress: WasmoFileAddress): Installer {
    return when (wasmoFileAddress) {
      is WasmoFileAddress.FileSystem -> TODO()
      is WasmoFileAddress.Http -> zipInstallerFactory.create(wasmoFileAddress)
    }
  }
}


