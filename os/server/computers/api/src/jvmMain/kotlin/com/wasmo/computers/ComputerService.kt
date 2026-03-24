package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.packaging.AppManifest
import okhttp3.HttpUrl

interface ComputerService {
  val id: ComputerId
  val slug: ComputerSlug
  val manifestLoader: ManifestLoader
  val url: HttpUrl

  /** Install default apps. */
  context(transactionCallbacks: TransactionCallbacks)
  fun initialize()

  context(transactionCallbacks: TransactionCallbacks)
  fun enqueueInstall(manifestAddress: ManifestAddress, manifest: AppManifest)

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): ComputerSnapshot
}

