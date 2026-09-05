package com.wasmo.distributions.homelab

import com.wasmo.accounts.AccountsBindings
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.accounts.passkeys.AccountsPasskeysBindings
import com.wasmo.calls.CallGraph
import com.wasmo.common.catalog.Catalog
import com.wasmo.common.logging.Logger
import com.wasmo.computers.ComputerServiceGraph
import com.wasmo.computers.ComputersBindings
import com.wasmo.computers.free.FreeComputersBindings
import com.wasmo.emails.EmailBindings
import com.wasmo.events.EventListener
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.HashingPepper
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.installedapps.InstalledAppServiceGraph
import com.wasmo.jobs.absurd.AbsurdBindings
import com.wasmo.ktor.KtorBindings
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.objectstore.s3.S3ObjectStoreBindings
import com.wasmo.passkeys.PasskeysBindings
import com.wasmo.passwords.PasswordBindings
import com.wasmo.permits.PermitsBindings
import com.wasmo.postgresqloperator.ExecPostgresqlOperatorBindings
import com.wasmo.postgresqloperator.LocalPostgresql
import com.wasmo.sendemail.postmark.PostmarkBindings
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sql.AppDatabaseSecret
import com.wasmo.sql.ProvisioningDb
import com.wasmo.sql.SqlServiceBindings
import com.wasmo.sql.WasmoPostgresqlConfig
import com.wasmo.usernames.UsernameBindings
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
    FreeComputersBindings::class,
    EmailBindings::class,
    ExecPostgresqlOperatorBindings::class,
    FileSystemObjectStoreBindings::class,
    HomelabMigratorBindings::class,
    InstalledAppBindings::class,
    KtorBindings::class,
    ObjectStoreBindings::class,
    PasskeysBindings::class,
    PasswordBindings::class,
    PermitsBindings::class,
    PostmarkBindings::class,
    S3ObjectStoreBindings::class,
    ServiceBindings::class,
    SqlServiceBindings::class,
    UsernameBindings::class,
    WebsiteBindings::class,
  ],
)
internal interface HomelabGraph {
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
      @Provides appDatabaseSecret: AppDatabaseSecret,
      @Provides deployment: Deployment,
      @Provides sessionCookieSpec: SessionCookieSpec,
      @Provides objectStoreAddress: ObjectStoreAddress,
      @Provides catalog: Catalog,
      @Provides postgresqlConfig: WasmoPostgresqlConfig,
      @Provides localPostgresql: LocalPostgresql,
      @Provides logger: Logger,
      @Provides eventListener: EventListener,
      @Provides hashingPepper: HashingPepper,
    ): HomelabGraph
  }
}
