@file:OptIn(ExperimentalStdlibApi::class)
@file:JvmName("HomelabWasmoOs")

package com.wasmo.distributions.homelab

import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.identifiers.Deployment
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sendemail.postmark.PostmarkProductionBaseUrl
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.ProvisioningDb
import com.wasmo.stripe.StripeCredentials
import com.wasmo.wiring.Distribution
import com.wasmo.wiring.WasmoService
import dev.zacsweers.metro.createGraphFactory
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import wasmo.sql.SqlDatabase

fun main(args: Array<String>): Unit = runBlocking {
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

  val homelabDistribution = object : Distribution() {
    override val osPostgresqlAddress: PostgresqlAddress
      get() = sharedPostgresqlAddress
    override val provisioningPostgresqlAddress: PostgresqlAddress
      get() = sharedPostgresqlAddress

    override fun createService(
      server: EmbeddedServer<*, *>,
      provisioningDb: ProvisioningDb,
      wasmoDb: SqlDatabase,
    ): WasmoService {
      val homelabGraphFactory = createGraphFactory<HomelabGraph.Factory>()
      val serviceGraph = homelabGraphFactory.create(
        server = server,
        cookieSecret = CookieSecret("butters".encodeUtf8()),
        postmarkCredentials = PostmarkCredentials(
          baseUrl = PostmarkProductionBaseUrl,
          serverToken = System.getenv("POSTMARK_SERVER_TOKEN") ?: "?",
        ),
        stripeCredentials = StripeCredentials(
          publishableKey = StripePublishableKey(stripePublishableKey),
          secretKey = stripeSecretKey,
        ),
        stripePublishableKey = StripePublishableKey(stripePublishableKey),
        catalog = DevelopmentCatalog,
        wasmoDb = wasmoDb,
        provisioningDb = provisioningDb,
        postgresqlAddress = sharedPostgresqlAddress,
        deployment = Deployment(
          baseUrl = "http://wasmo.localhost:8080/".toHttpUrl(),
          sendFromEmailAddress = "noreply@wasmo.dev",
        ),
        objectStoreAddress = FileSystemObjectStoreAddress(
          path = System.getProperty("user.home").toPath() / ".wasmo",
        ),
        sessionCookieSpec = SessionCookieSpec.Http,
      )
      return serviceGraph.wasmoService
    }
  }

  homelabDistribution.start(args)
}
