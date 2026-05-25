package com.wasmo.distributions.homelab

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.DistributionShortCode
import com.wasmo.ktor.WasmoKtorConfig
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.postgresqloperator.LocalPostgresql
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
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import wasmo.sql.SqlDatabase

class HomelabCommand : CliktCommand() {
  val container: Boolean by option("--container")
    .flag("--no-container", default = true)
  val stripePublishableKey: String by option()
    .defaultLazy { System.getenv("STRIPE_PUBLISHABLE_KEY") }
  val stripeSecretKey: String by option()
    .defaultLazy { System.getenv("STRIPE_SECRET_KEY") }

  override fun run() = runBlocking {
    val distribution = object : Distribution() {
      override val postgresqlConfig = WasmoPostgresqlConfig(
        hostname = "localhost",
        port = 54401,
        adminUser = "postgres",
        adminPassword = "password",
        adminDatabaseName = "postgres",
        osUser = "wasmo_development",
        osPassword = "password",
        osDatabaseName = "wasmo_development",
      )

      override val ktorConfig = WasmoKtorConfig(
        port = 54400,
      )

      override fun createService(
        server: EmbeddedServer<*, *>,
        provisioningDb: ProvisioningDb,
        wasmoDb: SqlDatabase,
      ): WasmoService {
        val localPostgresql = when (container) {
          true -> LocalPostgresql.Exec
          else -> LocalPostgresql.None
        }

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
          catalog = DevelopmentCatalog,
          wasmoDb = wasmoDb,
          provisioningDb = provisioningDb,
          postgresqlConfig = postgresqlConfig,
          deployment = Deployment(
            baseUrl = "http://wasmo.localhost:8080/".toHttpUrl(),
            sendFromEmailAddress = "noreply@wasmo.dev",
            distributionShortCode = DistributionShortCode("hl"),
          ),
          objectStoreAddress = FileSystemObjectStoreAddress(
            path = System.getProperty("user.home").toPath() / ".wasmo",
          ),
          sessionCookieSpec = SessionCookieSpec.Http,
          localPostgresql = localPostgresql,
        )
        return serviceGraph.wasmoService
      }
    }

    coroutineScope {
      distribution.start()
    }
  }
}
