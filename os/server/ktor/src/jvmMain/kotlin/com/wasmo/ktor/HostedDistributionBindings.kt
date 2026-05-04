package com.wasmo.ktor

import com.wasmo.computers.ComputerBindings
import com.wasmo.identifiers.OsScope
import com.wasmo.installedapps.InstalledAppBindings
import com.wasmo.objectstore.filesystem.FileSystemObjectStoreBindings
import com.wasmo.objectstore.s3.S3ObjectStoreBindings
import com.wasmo.sendemail.postmark.PostmarkBindings
import com.wasmo.sendemail.postmark.PostmarkCredentials
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer(
  includes = [
    PostmarkBindings::class,
    ComputerBindings::class,
    FileSystemObjectStoreBindings::class,
    HostedDistributionBindings::class,
    InstalledAppBindings::class,
    ServiceBindings::class,
    S3ObjectStoreBindings::class,
  ],
)
object HostedDistributionBindings {
  @Provides
  @SingleIn(OsScope::class)
  fun providePostmarkCredentials(config: WasmoService.Config): PostmarkCredentials =
    config.postmarkCredentials
}
