package com.wasmo.distributions.homelab

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.DevelopmentCatalog
import com.wasmo.events.LoggingEventListener
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.DistributionShortCode
import com.wasmo.identifiers.Secret
import com.wasmo.ktor.KtorLogger
import com.wasmo.ktor.WasmoKtorConfig
import com.wasmo.objectstore.FileSystemObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreAddress
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.debug.DebugProbes
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import wasmo.sql.SqlDatabase

class HomelabCommand : CliktCommand() {
  val container: Boolean by option("--container")
    .flag("--no-container", default = true)
  val devMode: Boolean by option("--dev-mode")
    .flag("--no-dev-mode", default = false)
  val stripePublishableKey: String by option()
    .defaultLazy {
      System.getenv("STRIPE_PUBLISHABLE_KEY")
        ?: "pk_UNKNOWN"
    }
  val stripeSecretKey: String by option()
    .defaultLazy {
      System.getenv("STRIPE_SECRET_KEY")
        ?: "sk_UNKNOWN"
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun run() = runBlocking {
    var baseUrl = "http://wasmo.localhost:8080/".toHttpUrl()

    val deployment = Deployment(
      baseUrl = baseUrl,
      sendFromEmailAddress = "noreply@wasmo.dev",
      distributionShortCode = DistributionShortCode("hl"),
    )

    var postgresqlConfig = WasmoPostgresqlConfig(
      hostname = "localhost",
      adminUser = "postgres",
      adminPassword = Secret("password"),
      adminDatabaseName = "postgres",
      osUser = "wasmo_homelab",
      osPassword = Secret("password"),
      osDatabaseName = "wasmo_homelab",
      appPrefix = deployment.distributionShortCode.shortCode,
    )

    var ktorConfig = WasmoKtorConfig()

    val objectStoreAddress: ObjectStoreAddress

    if (container) {
      baseUrl = baseUrl.newBuilder()
        .port(54400)
        .build()
      postgresqlConfig = postgresqlConfig.copy(
        port = 54401,
      )
      ktorConfig = ktorConfig.copy(
        port = 54400
      )
      objectStoreAddress = FileSystemObjectStoreAddress(
        path = "/wasmo/objectstore".toPath(),
      )
    } else {
      objectStoreAddress = FileSystemObjectStoreAddress(
        path = System.getProperty("user.home").toPath() / ".wasmo",
      )
    }

    if (devMode) {
      // Setup coroutines debugging.
      DebugProbes.install()
      DebugProbes.enableCreationStackTraces = true
      DebugProbes.sanitizeStackTraces = true
      System.setProperty(
        kotlinx.coroutines.DEBUG_PROPERTY_NAME,
        kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
      )
    }
    println("devMode: $devMode")
    println("DebugProbes.isInstalled: ${DebugProbes.isInstalled}")
    println("DebugProbes.enableCreationStackTraces: ${DebugProbes.enableCreationStackTraces}")
    println("${kotlinx.coroutines.DEBUG_PROPERTY_NAME}: ${System.getProperty(kotlinx.coroutines.DEBUG_PROPERTY_NAME)}")

    val distribution = object : Distribution() {
      override val postgresqlConfig = postgresqlConfig
      override val ktorConfig = ktorConfig

      override fun createService(
        server: EmbeddedServer<*, *>,
        provisioningDb: ProvisioningDb,
        wasmoDb: SqlDatabase,
      ): WasmoService {
        val logger = KtorLogger(server)
        val eventListener = LoggingEventListener(logger)
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
          deployment = deployment,
          objectStoreAddress = objectStoreAddress,
          sessionCookieSpec = SessionCookieSpec.Http,
          localPostgresql = localPostgresql,
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
