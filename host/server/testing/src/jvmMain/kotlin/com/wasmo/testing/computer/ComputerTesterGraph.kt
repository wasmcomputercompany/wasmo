package com.wasmo.testing.computer

import com.wasmo.computers.ComputerScope
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.installedapp.InstalledAppTesterGraph
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(
  scope = ComputerScope::class,
)
interface ComputerTesterGraph {
  val computerTester: ComputerTester
  val installedAppTesterGraphFactory: InstalledAppTesterGraph.Factory

  @GraphExtension.Factory
  interface Factory {
    fun create(
      @Provides slug: ComputerSlug,
    ): ComputerTesterGraph
  }
}
