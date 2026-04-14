package com.wasmo.installedapps

import com.wasmo.app.db2.WasmoDbTransaction as TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.app.db.InstalledApp
import com.wasmo.app.db.InstalledAppRelease
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId

interface InstalledAppStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(installedAppId: InstalledAppId): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService

  fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService
}
