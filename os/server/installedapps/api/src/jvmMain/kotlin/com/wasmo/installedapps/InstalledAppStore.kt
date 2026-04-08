package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.InstalledApp
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.packaging.AppManifest

interface InstalledAppStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(installedAppId: InstalledAppId): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(
    installedApp: InstalledApp,
    installedManifest: AppManifest,
  ): InstalledAppService

  fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedManifest: AppManifest,
  ): InstalledAppService
}
