@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerDevelopment")

package com.wasmo.ktor.development

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.deployment.Deployment
import com.wasmo.ktor.WasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import com.wasmo.stripe.StripeCredentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

fun main(args: Array<String>) {
  val stripePublishableKey = System.getenv("STRIPE_PUBLISHABLE_KEY")
    ?: error("required env STRIPE_PUBLISHABLE_KEY not set")
  val stripeSecretKey = System.getenv("STRIPE_SECRET_KEY")
    ?: error("required env STRIPE_SECRET_KEY not set")
  val service = WasmoService(
    cookieSecret = "butters".encodeUtf8(),
    postmarkCredentials = PostmarkCredentials(
      baseUrl = PostmarkProductionBaseUrl,
      serverToken = System.getenv("POSTMARK_SERVER_TOKEN") ?: "?",
    ),
    stripeCredentials = StripeCredentials(
      publishableKey = StripePublishableKey(stripePublishableKey),
      secretKey = stripeSecretKey,
    ),
    catalog = DevelopmentCatalog,
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
