package com.wasmo.installedapps

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.InstalledApp
import com.wasmo.db.InstalledAppRelease
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug

interface InstalledAppStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease,
  ): InstalledAppService

  context(transactionCallbacks: TransactionCallbacks)
  fun get(
    computerSlug: ComputerSlug,
    installedApp: InstalledApp,
    installedAppRelease: InstalledAppRelease,
  ): InstalledAppService
}
