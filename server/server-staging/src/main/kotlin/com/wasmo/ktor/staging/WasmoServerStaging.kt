@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerStaging")

package com.wasmo.ktor.staging

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.ktor.WasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.decodeHex
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val cookieSecret = System.getenv("COOKIE_SECRET")
    ?: error("required env COOKIE_SECRET not set")
  val service = WasmoService(
    cookieSecret = cookieSecret.decodeHex(),
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_staging",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    baseUrl = "https://wasmo.dev/".toHttpUrl(),
    objectStoreAddress = FileSystemObjectStoreAddress(
      fileSystem = FileSystem.SYSTEM,
      path = System.getProperty("user.home").toPath() / ".wasmo",
    ),
    sessionCookieSpec = SessionCookieSpec.Https,
  )
  service.start(args = args)
}
