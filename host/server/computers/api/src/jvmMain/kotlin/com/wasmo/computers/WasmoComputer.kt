package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.db.AppInstall
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl

interface WasmoComputer {
  val id: ComputerId
  val slug: ComputerSlug
  val manifestLoader: ManifestLoader
  val appInstaller: AppInstaller

  /** Install default apps. */
  context(transactionCallbacks: TransactionCallbacks)
  fun initialize()

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): ComputerSnapshot
}

interface ManifestLoader {
  suspend fun loadManifest(manifestUrl: HttpUrl): AppManifest
}

interface AppInstaller {
  context(transactionCallbacks: TransactionCallbacks)
  fun enqueueInstall(
    manifestUrl: HttpUrl,
    manifest: AppManifest,
  )

  suspend fun install(appInstall: AppInstall)
}
