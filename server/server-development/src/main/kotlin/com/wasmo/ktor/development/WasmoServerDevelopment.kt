@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerDevelopment")

package com.wasmo.ktor.development

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.deployment.Deployment
import com.wasmo.ktor.WasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val service = WasmoService(
    cookieSecret = "butters".encodeUtf8(),
    postmarkCredentials = PostmarkCredentials(
      baseUrl = PostmarkProductionBaseUrl,
      serverToken = System.getenv("POSTMARK_SERVER_TOKEN") ?: "?",
    ),
    postgresDatabaseHostname = "localhost",
    postgresDatabaseName = "wasmcomputer_development",
    postgresDatabaseUser = "postgres",
    postgresDatabasePassword = "password",
    deployment = Deployment(
      baseUrl = "http://localwasmo:8080/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.dev",
    ),
    objectStoreAddress = FileSystemObjectStoreAddress(
      fileSystem = FileSystem.SYSTEM,
      path = System.getProperty("user.home").toPath() / ".wasmo",
    ),
    sessionCookieSpec = SessionCookieSpec.Http,
  )
  service.start(args = args)
}
