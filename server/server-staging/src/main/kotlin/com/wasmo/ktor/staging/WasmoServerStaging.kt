@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerStaging")

package com.wasmo.ktor.staging

import com.wasmo.ktor.WasmoService
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val service = WasmoService(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_staging",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    baseUrl = "https://staging.wasmo.com/".toHttpUrl(),
    fileSystem = FileSystem.SYSTEM,
    path = System.getProperty("user.home").toPath() / ".wasmo",
  )
  service.start(args = args)
}
