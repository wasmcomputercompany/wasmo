package com.wasmo.installedapps

import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
interface InstalledAppBindings {
  @Binds
  fun bindInstalledAppStore(real: RealInstalledAppStore): InstalledAppStore

  companion object {
    @Provides
    @SingleIn(OsScope::class)
    fun provideInstallAppJobQueue(
      jobQueueFactory: OsJobQueue.Factory,
    ): OsJobQueue<InstallAppJob> = jobQueueFactory.create(InstallAppJob.JobName)

    @Provides
    @IntoSet
    @SingleIn(OsScope::class)
    fun provideInstallAppJobRegistration(
      installAppJobHandler: InstallAppJobHandler,
    ): JobRegistration<*, *> = JobRegistration(InstallAppJob.JobName, installAppJobHandler)

    @Provides
    @SingleIn(OsScope::class)
    fun provideApplicationJobQueue(
      jobQueueFactory: OsJobQueue.Factory,
    ): OsJobQueue<ApplicationJob> = jobQueueFactory.create(ApplicationJob.JobName)

    @Provides
    @IntoSet
    @SingleIn(OsScope::class)
    fun provideApplicationJobRegistration(
      applicationJobHandler: ApplicationJobHandler,
    ): JobRegistration<*, *> = JobRegistration(ApplicationJob.JobName, applicationJobHandler)
  }
}
