package com.wasmo.computers

import com.wasmo.api.CreateComputerSpecRequest
import com.wasmo.api.CreateComputerSpecResponse
import com.wasmo.api.InstallAppRequest
import com.wasmo.api.InstallAppResponse
import com.wasmo.framework.ActionRegistration
import com.wasmo.framework.HttpRequestPattern
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.JobName
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstallAppJob
import com.wasmo.jobs.JobRegistration
import com.wasmo.jobs.OsJobQueue
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ElementsIntoSet
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
abstract class ComputersBindings {

  @Binds
  abstract fun bindComputerStore(real: RealComputerStore): ComputerStore

  companion object {
    private val InstallAppJobName = JobName<InstallAppJob, Unit>("InstallAppJob")

    @Provides
    @ElementsIntoSet
    @SingleIn(OsScope::class)
    fun provideActionRegistrations(
      hostnamePatterns: HostnamePatterns,
    ): List<ActionRegistration> = listOf(
      ActionRegistration.Rpc<CreateComputerSpecRequest, CreateComputerSpecResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.osHostname,
          path = "/create-computer-spec",
        ),
        action = CreateComputerSpecRpc::class,
      ),

      ActionRegistration.Rpc<InstallAppRequest, InstallAppResponse>(
        pattern = HttpRequestPattern(
          host = hostnamePatterns.computerRegex,
          path = "/install-app",
        ),
        action = InstallAppRpc::class,
      ),
    )

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
  }
}
