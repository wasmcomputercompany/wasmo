package com.wasmo.ktor

import com.wasmo.app.db.WasmoDbService
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.netty.EngineMain

fun startWasmoService(
  config: WasmoServiceConfig,
  args: Array<String>,
): WasmoService {
  val server = EngineMain.createServer(args)

  val wasmoDb = WasmoDbService.start(
    hostname = config.postgresDatabaseHostname,
    databaseName = config.postgresDatabaseName,
    user = config.postgresDatabaseUser,
    password = config.postgresDatabasePassword,
    ssl = false,
  )

  val appGraphFactory = createGraphFactory<AppGraph.Factory>()
  val appGraph = appGraphFactory.create(
    config = config,
    server = server,
    wasmoDb = wasmoDb,
  )

  appGraph.wasmoService.start()
  return appGraph.wasmoService
}
