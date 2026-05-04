package com.wasmo.installedapps

import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class InstalledAppBindings {
  @Binds
  abstract fun bindInstalledAppStore(real: RealInstalledAppStore): InstalledAppStore

  companion object {
    private val InstallAppJobName = JobName<InstallAppJob, Unit>("InstallAppJob")
    private val ApplicationJobName = JobName<ApplicationJob, Unit>("ApplicationJob")

    @Provides
    @SingleIn(OsScope::class)
    fun provideInstallAppJobQueue(
      jobQueueFactory: OsJobQueue.Factory,
    ): OsJobQueue<InstallAppJob> = jobQueueFactory.create(InstallAppJobName)

    @Provides
    @IntoSet
    @SingleIn(OsScope::class)
    fun provideInstallAppJobRegistration(
      installAppJobHandler: InstallAppJobHandler,
    ): JobRegistration<*, *> = JobRegistration(InstallAppJobName, installAppJobHandler)

    @Provides
    @SingleIn(OsScope::class)
    fun provideApplicationJobQueue(
      jobQueueFactory: OsJobQueue.Factory,
    ): OsJobQueue<ApplicationJob> = jobQueueFactory.create(ApplicationJobName)

    @Provides
    @IntoSet
    @SingleIn(OsScope::class)
    fun provideApplicationJobRegistration(
      applicationJobHandler: ApplicationJobHandler,
    ): JobRegistration<*, *> = JobRegistration(ApplicationJobName, applicationJobHandler)
  }
}
