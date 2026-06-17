package com.wasmo.distributions.hosted

import com.github.ajalt.clikt.core.CliktCommand
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.DistributionShortCode
import com.wasmo.identifiers.Secret
import com.wasmo.ktor.WasmoKtorConfig
import com.wasmo.objectstore.BackblazeB2BucketAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.stripe.StripeCredentials
import com.wasmo.wiring.Distribution
import com.wasmo.wiring.WasmoService
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.decodeHex
import wasmo.sql.SqlDatabase

class HostedCommand : CliktCommand() {
  override fun run() = runBlocking {
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

    val deployment = Deployment(
      baseUrl = "https://wasmo.com/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.com",
      distributionShortCode = DistributionShortCode("wc"),
    )

    val distribution = object : Distribution() {
      override val postgresqlConfig = WasmoPostgresqlConfig(
        hostname = "gcp-northamerica-northeast1-1.pg.psdb.cloud",
        ssl = false,
        adminUser = "pscale_api_eu3kxhe4lp41.7q408njs9kb7",
        adminPassword = Secret(postgresDatabasePassword),
        adminDatabaseName = "postgres",
        osUser = "pscale_api_eu3kxhe4lp41.7q408njs9kb7",
        osPassword = Secret(postgresDatabasePassword),
        osDatabaseName = "wasmo_com",
        appPrefix = deployment.distributionShortCode.shortCode,
      )

      override val ktorConfig = WasmoKtorConfig()

      override fun createService(
        server: EmbeddedServer<*, *>,
        provisioningDb: ProvisioningDb,
        wasmoDb: SqlDatabase,
      ): WasmoService {
        val hostedGraphFactory = createGraphFactory<HostedGraph.Factory>()
        val serviceGraph = hostedGraphFactory.create(
          server = server,
          cookieSecret = CookieSecret(cookieSecret.decodeHex()),
          postmarkCredentials = PostmarkCredentials(
            baseUrl = PostmarkProductionBaseUrl,
            serverToken = postmarkServerToken,
          ),
          stripeCredentials = StripeCredentials(
            publishableKey = StripePublishableKey(stripePublishableKey),
            secretKey = stripeSecretKey,
          ),
          catalog = DevelopmentCatalog,
          wasmoDb = wasmoDb,
          provisioningDb = provisioningDb,
          postgresqlConfig = postgresqlConfig,
          deployment = deployment,
          objectStoreAddress = BackblazeB2BucketAddress(
            regionId = b2RegionId,
            applicationKeyId = b2ApplicationKeyId,
            applicationKey = b2ApplicationKey,
            bucket = b2Bucket,
          ),
          sessionCookieSpec = SessionCookieSpec.Https,
          logger = logger,
          eventListener = eventListener,
        )
        return serviceGraph.wasmoService
      }
    }

    coroutineScope {
      distribution.start()
    }
  }
}
