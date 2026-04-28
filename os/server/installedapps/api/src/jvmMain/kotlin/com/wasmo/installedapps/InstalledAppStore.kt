package com.wasmo.installedapps

import com.wasmo.accounts.Client
import com.wasmo.db.installedapps.DbInstalledApp
import com.wasmo.db.installedapps.DbInstalledAppRelease
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.InstalledAppId
import com.wasmo.sql.SqlTransaction
import wasmo.access.Caller

interface InstalledAppStore {
  /** Note that this returns the HTTP service even if the current client is not its owner. */
  context(sqlTransaction: SqlTransaction)
  suspend fun getHttpServiceAndAccessOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): Pair<InstalledAppHttpService, Caller>?

  /** Returns null unless [client] is the owner of the computer. */
  context(sqlTransaction: SqlTransaction)
  suspend fun getOrNull(
    client: Client,
    computerSlug: ComputerSlug,
    appSlug: AppSlug,
  ): InstalledAppService?

  context(sqlTransaction: SqlTransaction)
  suspend fun get(
    installedAppId: InstalledAppId,
  ): InstalledAppService?

  context(sqlTransaction: SqlTransaction)
  suspend fun get(
    installedApp: DbInstalledApp,
    installedAppRelease: DbInstalledAppRelease?,
  ): InstalledAppService

  suspend fun get(
    computerSlug: ComputerSlug,
    installedApp: DbInstalledApp,
    installedAppRelease: DbInstalledAppRelease?,
  ): InstalledAppService
}
