@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServiceDevelopment")

package com.wasmo.ktor.development

import com.wasmo.ktor.WasmoService
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val service = WasmoService(
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_development",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    baseUrl = "http://localwasmo:8080/".toHttpUrl(),
    fileSystem = FileSystem.SYSTEM,
    path = System.getProperty("user.home").toPath() / ".wasmo",
  )
  service.start(args = args)
}
