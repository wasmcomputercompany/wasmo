package com.wasmo.installedapps

import com.wasmo.accounts.Client
import com.wasmo.db.installedapps.InstalledApp
import com.wasmo.db.installedapps.InstalledAppRelease
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.sql.SqlTransaction

interface InstalledAppStore {
  context(sqlTransaction: SqlTransaction)
  suspend fun getOrNull(client: Client, computerSlug: ComputerSlug, appSlug: AppSlug): InstalledAppService?

  context(sqlTransaction: SqlTransaction)
  suspend fun get(installedAppId: InstalledAppId): InstalledAppService?

  context(sqlTransaction: SqlTransaction)
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
