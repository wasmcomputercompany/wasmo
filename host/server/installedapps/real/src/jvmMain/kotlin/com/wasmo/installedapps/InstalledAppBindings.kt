package com.wasmo.installedapps

import com.wasmo.jobs.JobExecutor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.serialization.KSerializer

@BindingContainer
interface InstalledAppBindings {
  @Binds
  fun bindInstalledAppStore(real: RealInstalledAppStore): InstalledAppStore

  @Binds
  fun bindJobExecutor(real: InstallAppJobExecutor): JobExecutor<InstallAppJob>

  companion object {
    @Provides
    @SingleIn(AppScope::class)
    fun provideInstallAppJobSerializer(): KSerializer<InstallAppJob> = InstallAppJob.serializer()
  }
}
