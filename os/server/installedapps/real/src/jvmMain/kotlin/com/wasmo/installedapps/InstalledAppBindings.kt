package com.wasmo.installedapps

import com.wasmo.jobs.OsJobHandler
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
interface InstalledAppBindings {
  @Binds
  fun bindInstalledAppStore(real: RealInstalledAppStore): InstalledAppStore

  @Binds
  fun bindInstallAppJobHandler(real: InstallAppJobHandler): OsJobHandler<InstallAppJob, Unit>
}
