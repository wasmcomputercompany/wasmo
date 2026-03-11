package com.wasmo.computers

import app.cash.sqldelight.TransactionCallbacks
import com.wasmo.accounts.Client
import com.wasmo.db.InstalledApp
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.installedapps.InstalledAppService

interface InstalledAppStore {
  context(transactionCallbacks: TransactionCallbacks)
  fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(transactionCallbacks: TransactionCallbacks)
  fun get(installedApp: InstalledApp): InstalledAppService

  context(transactionCallbacks: TransactionCallbacks)
  fun get(computerSlug: ComputerSlug, installedApp: InstalledApp): InstalledAppService
}
