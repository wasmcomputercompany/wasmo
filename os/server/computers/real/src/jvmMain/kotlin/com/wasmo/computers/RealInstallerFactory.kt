package com.wasmo.computers

import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerScope
import com.wasmo.identifiers.WasmoFileAddress
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(ComputerScope::class)
class RealInstallerFactory(
  private val fileSystemResourceInstallerFactory: FileSystemResourceInstaller.Factory,
  private val zipInstallerFactory: ZipResourceInstaller.Factory,
) : ResourceInstaller.Factory {
  override fun create(
    appSlug: AppSlug,
    wasmoFileAddress: WasmoFileAddress,
  ): ResourceInstaller {
    return when (wasmoFileAddress) {
      is WasmoFileAddress.FileSystem -> fileSystemResourceInstallerFactory.create(wasmoFileAddress)
      is WasmoFileAddress.Http -> zipInstallerFactory.create(appSlug, wasmoFileAddress)
    }
  }
}
