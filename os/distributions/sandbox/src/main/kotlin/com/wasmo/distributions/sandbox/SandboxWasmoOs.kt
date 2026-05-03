@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("SandboxWasmoOs")

package com.wasmo.distributions.sandbox

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.AccountType
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.deployment.Deployment
import com.wasmo.ktor.WasmoService
import com.wasmo.ktor.startWasmoService
import com.wasmo.objectstore.BackblazeB2BucketAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.stripe.StripeCredentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.decodeHex

suspend fun main(args: Array<String>) {
  val cookieSecret = System.getenv("COOKIE_SECRET")
    ?: error("required env COOKIE_SECRET not set")

  val postmarkServerToken = System.getenv("POSTMARK_SERVER_TOKEN")
    ?: error("required env POSTMARK_SERVER_TOKEN not set")

  val b2RegionId = System.getenv("B2_REGION_ID")
    ?: error("required env B2_REGION_ID not set")
  val b2ApplicationKeyId = System.getenv("B2_APPLICATION_KEY_ID")
    ?: error("required env B2_APPLICATION_KEY_ID not set")
  val b2ApplicationKey = System.getenv("B2_APPLICATION_KEY")
    ?: error("required env B2_APPLICATION_KEY not set")
  val b2Bucket = System.getenv("B2_BUCKET")
    ?: error("required env B2_BUCKET not set")

  val stripePublishableKey = System.getenv("STRIPE_PUBLISHABLE_KEY")
    ?: error("required env STRIPE_PUBLISHABLE_KEY not set")
  val stripeSecretKey = System.getenv("STRIPE_SECRET_KEY")
    ?: error("required env STRIPE_SECRET_KEY not set")

  val postgresDatabasePassword = System.getenv("PGPASSWORD")
    ?: error("required env PGPASSWORD not set")

  val sharedPostgresqlAddress = PostgresqlAddress(
    user = "pscale_api_uh85t8q0waqt.hkqtmgf3pdzi",
    password = postgresDatabasePassword,
    hostname = "gcp-northamerica-northeast1-1.pg.psdb.cloud",
    databaseName = "wasmo_dev",
    ssl = false,
  )
  val config = WasmoService.Config(
    accountType = AccountType.Standard,
    cookieSecret = cookieSecret.decodeHex(),
    postmarkCredentials = PostmarkCredentials(
      baseUrl = PostmarkProductionBaseUrl,
      serverToken = postmarkServerToken,
    ),
    stripeCredentials = StripeCredentials(
      publishableKey = StripePublishableKey(stripePublishableKey),
      secretKey = stripeSecretKey,
    ),
    catalog = DevelopmentCatalog,
    osPostgresqlAddress = sharedPostgresqlAddress,
    provisioningPostgresqlAddress = sharedPostgresqlAddress,
    deployment = Deployment(
      baseUrl = "https://wasmo.dev/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.dev",
    ),
    objectStoreAddress = BackblazeB2BucketAddress(
      regionId = b2RegionId,
      applicationKeyId = b2ApplicationKeyId,
      applicationKey = b2ApplicationKey,
      bucket = b2Bucket,
    ),
    sessionCookieSpec = SessionCookieSpec.Https,
  )
  startWasmoService(config, args)
}
