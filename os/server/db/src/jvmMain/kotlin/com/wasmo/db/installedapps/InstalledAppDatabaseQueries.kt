package com.wasmo.db.installedapps

import com.wasmo.db.bindDatabaseSlug
import com.wasmo.db.bindInstalledAppId
import com.wasmo.db.getDatabaseSlug
import com.wasmo.db.getInstalledAppDatabaseId
import com.wasmo.db.getInstalledAppId
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.InstalledAppDatabaseId
import com.wasmo.identifiers.InstalledAppId
import kotlin.time.Instant
import okio.ByteString
import wasmo.sql.SqlConnection
import wasmo.sql.SqlRow
import wasmox.sql.list
import wasmox.sql.single

context(connection: SqlConnection)
suspend fun insertInstalledAppDatabase(
  installedAppId: InstalledAppId,
  slug: DatabaseSlug,
  createdAt: Instant,
  version: Long,
  credential: ByteString,
): InstalledAppDatabaseId {
  val rowIterator = connection.executeQuery(
    """
    INSERT INTO InstalledAppDatabase(
      installed_app_id,
      slug,
      created_at,
      version,
      credential
    )
    VALUES (
      $1,
      $2,
      $3,
      $4,
      $5
    ) RETURNING id
    """,
  ) {
    bindInstalledAppId(0, installedAppId)
    bindDatabaseSlug(1, slug)
    bindInstant(2, createdAt)
    bindS64(3, version)
    bindBytes(4, credential)
  }
  return rowIterator.single {
    getInstalledAppDatabaseId(0)
  }
}

context(connection: SqlConnection)
suspend fun selectInstalledAppDatabaseByInstalledAppDbSlug(
  installedAppId: InstalledAppId,
  slug: DatabaseSlug,
): DbInstalledAppDatabase? {
  val rowIterator = connection.executeQuery(
    """
    SELECT
      id,
      installed_app_id,
      slug,
      created_at,
      version,
      credential
    FROM InstalledAppDatabase
    WHERE
      installed_app_id = $1 AND
      slug = $2
    """,
  ) {
    bindInstalledAppId(0, installedAppId)
    bindDatabaseSlug(1, slug)
  }

  return rowIterator.list {
    getInstalledAppDatabase()
  }.singleOrNull() // This query is on a unique index.
}

private fun SqlRow.getInstalledAppDatabase() = DbInstalledAppDatabase(
  id = getInstalledAppDatabaseId(0),
  installedAppId = getInstalledAppId(1),
  slug = getDatabaseSlug(2),
  createdAt = getInstant(3)!!,
  version = getS64(4)!!,
  credential = getBytes(5)!!
)

