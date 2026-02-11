@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerProduction")

package com.wasmo.ktor.production

import com.wasmo.ktor.WasmoService
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val service = WasmoService(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_production",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    baseUrl = "https://wasmo.com/".toHttpUrl(),
    fileSystem = FileSystem.SYSTEM,
    path = System.getProperty("user.home").toPath() / ".wasmo",
  )
  service.start(args = args)
}
