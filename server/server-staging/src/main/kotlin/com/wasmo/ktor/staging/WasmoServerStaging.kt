@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerStaging")

package com.wasmo.ktor.staging

import com.wasmo.ktor.WasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val service = WasmoService(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_staging",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    baseUrl = "https://wasmo.dev/".toHttpUrl(),
    objectStoreAddress = FileSystemObjectStoreAddress(
      fileSystem = FileSystem.SYSTEM,
      path = System.getProperty("user.home").toPath() / ".wasmo",
    ),
  )
  service.start(args = args)
}
