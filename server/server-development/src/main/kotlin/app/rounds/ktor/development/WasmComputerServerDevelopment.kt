@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmComputerServerDevelopment")

package app.rounds.ktor.development

import app.rounds.ktor.WasmComputerServer

fun main(args: Array<String>) {
  val server = WasmComputerServer(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_development",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
  )
  server.start(
    withWebSockets = true,
    args = args,
  )
}
