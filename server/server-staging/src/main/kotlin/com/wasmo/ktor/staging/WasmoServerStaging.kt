@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerStaging")

package com.wasmo.ktor.staging

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.deployment.Deployment
import com.wasmo.ktor.WasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.decodeHex
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val cookieSecret = System.getenv("COOKIE_SECRET")
    ?: error("required env COOKIE_SECRET not set")
  val postmarkServerToken = System.getenv("POSTMARK_SERVER_TOKEN")
    ?: error("required env POSTMARK_SERVER_TOKEN not set")
  val service = WasmoService(
    cookieSecret = cookieSecret.decodeHex(),
    postmarkCredentials = PostmarkCredentials(
      baseUrl = PostmarkProductionBaseUrl,
      serverToken = postmarkServerToken,
    ),
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_staging",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    deployment = Deployment(
      baseUrl = "https://wasmo.dev/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.dev",
    ),
    objectStoreAddress = FileSystemObjectStoreAddress(
      fileSystem = FileSystem.SYSTEM,
      path = System.getProperty("user.home").toPath() / ".wasmo",
    ),
    sessionCookieSpec = SessionCookieSpec.Https,
  )
  service.start(args = args)
}
