package com.wasmo.ktor

import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.common.catalog.Catalog
import com.wasmo.computers.ComputerBindings
import com.wasmo.deployment.Deployment
import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.jobs.absurd.AbsurdBindings
import com.wasmo.objectstore.ObjectStoreFactory
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.objectstore.s3.S3ObjectStoreBindings
import com.wasmo.passkeys.PasskeysBindings
import com.wasmo.sendemail.postmark.PostmarkBindings
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.sql.PostgresqlAddress
import com.wasmo.sql.SqlServiceBindings
import com.wasmo.stripe.StripeBindings
import com.wasmo.stripe.StripeCredentials
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import wasmo.objectstore.ObjectStore

@BindingContainer(
  includes = [
    AbsurdBindings::class,
    ComputerBindings::class,
    FileSystemObjectStoreBindings::class,
    HostedDistributionBindings::class,
    InstalledAppBindings::class,
    PasskeysBindings::class,
    PostmarkBindings::class,
    S3ObjectStoreBindings::class,
    ServiceBindings::class,
    SqlServiceBindings::class,
    StripeBindings::class,
  ],
)
object HostedDistributionBindings {
  @Provides
  @SingleIn(OsScope::class)
  fun providePostmarkCredentials(config: WasmoService.Config): PostmarkCredentials =
    config.postmarkCredentials

  @Provides
  @SingleIn(OsScope::class)
  fun provideCookieSecret(config: WasmoService.Config): CookieSecret =
    CookieSecret(config.cookieSecret)

  @Provides
  @SingleIn(OsScope::class)
  fun provideDeployment(config: WasmoService.Config): Deployment =
    config.deployment

  @Provides
  @SingleIn(OsScope::class)
  fun provideSessionCookieSpec(config: WasmoService.Config): SessionCookieSpec =
    config.sessionCookieSpec

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripePublishableKey(config: WasmoService.Config): StripePublishableKey =
    config.stripeCredentials.publishableKey

  @Provides
  @SingleIn(OsScope::class)
  fun provideStripeCredentials(config: WasmoService.Config): StripeCredentials = config.stripeCredentials

  @Provides
  @ForOs
  @SingleIn(OsScope::class)
  fun provideObjectStore(
    config: WasmoService.Config,
    objectStoreFactory: ObjectStoreFactory,
  ): ObjectStore = objectStoreFactory.open(config.objectStoreAddress)

  @Provides
  @SingleIn(OsScope::class)
  fun provideCatalog(config: WasmoService.Config): Catalog = config.catalog

  @Provides
  @SingleIn(OsScope::class)
  fun providePostgresqlAddress(config: WasmoService.Config): PostgresqlAddress =
    config.osPostgresqlAddress
}
