package com.wasmo.computers.packaging

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class PackagingBindings {

  @Binds
  internal abstract fun bindInstallerFactory(
    real: RealInstallerFactory,
  ): Installer.Factory

  @Binds
  internal abstract fun bindResourceLoaderFactory(
    real: RealResourceLoaderFactory,
  ): ResourceLoader.Factory
}
