@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("WasmoServerDevelopment")

package com.wasmo.ktor.development

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.deployment.Deployment
import com.wasmo.ktor.WasmoService
import com.wasmo.ktor.startWasmoService
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.stripe.StripeCredentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath

fun main(args: Array<String>) {
  val stripePublishableKey = System.getenv("STRIPE_PUBLISHABLE_KEY")
    ?: error("required env STRIPE_PUBLISHABLE_KEY not set")
  val stripeSecretKey = System.getenv("STRIPE_SECRET_KEY")
    ?: error("required env STRIPE_SECRET_KEY not set")
  val sharedPostgresqlAddress = PostgresqlAddress(
    user = "postgres",
    password = "password",
    hostname = "localhost",
    databaseName = "wasmo_development",
    ssl = false,
  )
  val config = WasmoService.Config(
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
    hostPostgresqlAddress = sharedPostgresqlAddress,
    guestPostgresqlAddress = sharedPostgresqlAddress,
    deployment = Deployment(
      baseUrl = "http://wasmo.localhost:8080/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.dev",
    ),
    objectStoreAddress = FileSystemObjectStoreAddress(
      path = System.getProperty("user.home").toPath() / ".wasmo",
    ),
    sessionCookieSpec = SessionCookieSpec.Http,
  )
  startWasmoService(config, args)
}
