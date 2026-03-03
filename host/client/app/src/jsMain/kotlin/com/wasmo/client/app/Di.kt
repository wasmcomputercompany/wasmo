package com.wasmo.client.app

import com.wasmo.common.logging.ConsoleLogger
import com.wasmo.common.logging.Logger
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

fun createWasmoClientApp(
  logger: Logger = ConsoleLogger,
  environment: Environment,
): WasmoClientApp {
  val graph = createGraphFactory<WasmoClientAppGraph.Factory>()
    .create(logger, environment)
  return graph.wasmoClientApp
}

@DependencyGraph
interface WasmoClientAppGraph {
  val wasmoClientApp: WasmoClientApp

  @DependencyGraph.Factory
  fun interface Factory {
    fun create(
      @Provides logger: Logger,
      @Provides environment: Environment,
    ): WasmoClientAppGraph
  }
}
