package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.api.ComputerSnapshot
import com.wasmo.computers.packaging.Installer
import com.wasmo.identifiers.AppManifestAddress
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerId
import com.wasmo.identifiers.ComputerSlug
import okhttp3.HttpUrl

interface ComputerService {
  val id: ComputerId
  val slug: ComputerSlug
  val installerFactory: Installer.Factory
  val url: HttpUrl

  /** Install default apps. */
  context(transactionCallbacks: TransactionCallbacks)
  fun initialize()

  context(transactionCallbacks: TransactionCallbacks)
  fun enqueueInstall(
    appManifestAddress: AppManifestAddress,
    slug: AppSlug,
  )

  context(transactionCallbacks: TransactionCallbacks)
  fun snapshot(): ComputerSnapshot
}

