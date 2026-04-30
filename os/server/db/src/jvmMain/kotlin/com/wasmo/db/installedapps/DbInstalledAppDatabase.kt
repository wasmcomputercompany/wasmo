package com.wasmo.db.installedapps

import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppDatabaseId
import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import okio.ByteString

data class DbInstalledAppDatabase(
  val id: InstalledAppDatabaseId,
  val installedAppId: InstalledAppId,
  val slug: DatabaseSlug,
  val createdAt: Instant,
  val version: Long,
  val credential: ByteString,
)
