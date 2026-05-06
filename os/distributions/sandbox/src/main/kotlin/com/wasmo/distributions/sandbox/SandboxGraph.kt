package com.wasmo.distributions.sandbox

import com.wasmo.accounts.AccountsBindings
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.accounts.passkeys.AccountsPasskeysBindings
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.calls.CallGraph
import com.wasmo.common.catalog.Catalog
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.computers.ComputersBindings
import com.wasmo.emails.EmailBindings
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.jobs.absurd.AbsurdBindings
import com.wasmo.ktor.KtorBindings
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.objectstore.s3.S3ObjectStoreBindings
import com.wasmo.passkeys.PasskeysBindings
import com.wasmo.permits.PermitsBindings
import com.wasmo.sendemail.postmark.PostmarkBindings
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.SqlServiceBindings
import com.wasmo.stripe.StripeBindings
import com.wasmo.stripe.StripeCredentials
import com.wasmo.website.WebsiteBindings
import com.wasmo.wiring.ObjectStoreBindings
import com.wasmo.wiring.ServiceBindings
import com.wasmo.wiring.WasmoService
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.ktor.server.engine.EmbeddedServer
import wasmo.sql.SqlDatabase

@DependencyGraph(
  scope = OsScope::class,
  bindingContainers = [
    AbsurdBindings::class,
    AccountsBindings::class,
    AccountsPasskeysBindings::class,
    ComputersBindings::class,
    EmailBindings::class,
    FileSystemObjectStoreBindings::class,
    InstalledAppBindings::class,
    KtorBindings::class,
    ObjectStoreBindings::class,
    PasskeysBindings::class,
    PermitsBindings::class,
    PostmarkBindings::class,
    S3ObjectStoreBindings::class,
    ServiceBindings::class,
    SqlServiceBindings::class,
    StripeBindings::class,
    WebsiteBindings::class,
  ],
)
internal interface SandboxGraph {
  val wasmoService: WasmoService
  val callGraphFactory: CallGraph.Factory
  val computerServiceGraphFactory: ComputerServiceGraph.Factory
  val installedAppServiceGraphFactory: InstalledAppServiceGraph.Factory

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides server: EmbeddedServer<*, *>,
      @Provides wasmoDb: SqlDatabase,
      @Provides provisioningDb: ProvisioningDb,
      @Provides postmarkCredentials: PostmarkCredentials,
      @Provides cookieSecret: CookieSecret,
      @Provides deployment: Deployment,
      @Provides sessionCookieSpec: SessionCookieSpec,
      @Provides stripePublishableKey: StripePublishableKey,
      @Provides stripeCredentials: StripeCredentials,
      @Provides objectStoreAddress: ObjectStoreAddress,
      @Provides catalog: Catalog,
      @Provides postgresqlAddress: PostgresqlAddress,
    ): SandboxGraph
  }
}
