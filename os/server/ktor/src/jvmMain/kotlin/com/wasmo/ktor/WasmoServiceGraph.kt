package com.wasmo.ktor

import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.sql.ProvisioningDb
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.ktor.server.engine.EmbeddedServer
import wasmo.sql.SqlDatabase

@DependencyGraph(
  scope = OsScope::class,
  bindingContainers = [
    HostedDistributionBindings::class,
  ],
)
internal interface WasmoServiceGraph {
  val wasmoService: WasmoService
  val callGraphFactory: CallGraph.Factory
  val computerServiceGraphFactory: ComputerServiceGraph.Factory
  val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides config: WasmoService.Config,
      @Provides server: EmbeddedServer<*, *>,
      @Provides wasmoDb: SqlDatabase,
      @Provides provisioningDb: ProvisioningDb,
    ): WasmoServiceGraph
  }
}
