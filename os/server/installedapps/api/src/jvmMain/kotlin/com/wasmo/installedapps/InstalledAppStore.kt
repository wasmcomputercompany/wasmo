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
  suspend fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun get(installedAppId: InstalledAppId): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  suspend fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService

  suspend fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease?,
  ): InstalledAppService
}
