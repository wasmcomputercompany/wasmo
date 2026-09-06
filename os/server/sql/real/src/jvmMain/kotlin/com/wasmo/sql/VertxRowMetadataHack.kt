package com.wasmo.sql

import com.wasmo.identifiers.OsScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.vertx.pgclient.impl.RowImpl
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.desc.ColumnDescriptor
import io.vertx.sqlclient.internal.RowDesc
import java.lang.reflect.InaccessibleObjectException

/**
 * Our Vert.x Postgresql client knows the types of each rows' column values, but this data is not
 * exposed in the public API. Our SQL API needs to encode rows with their type information, so we
 * use a reflection hack to access a field that's not in the public API.
 */
@Inject
@SingleIn(OsScope::class)
class VertxRowMetadataHack {
  private val rowImplDesc = try {
    RowImpl::class.java.getDeclaredField("desc")
      .apply {
        isAccessible = true
      }
  } catch (e: NoSuchFieldException) {
    throw IllegalStateException("VertxRowMetadataHack failed: unexpected Vertx class structure", e)
  } catch (e: InaccessibleObjectException) {
    throw IllegalStateException("VertxRowMetadataHack failed: attempt to set privates failed", e)
  }

  fun getColumnDescriptors(row: Row): List<ColumnDescriptor> {
    require(row is RowImpl) { "VertxRowMetadataHack failed: unexpected Vertx row type" }
    val rowDesc = rowImplDesc.get(row) as? RowDesc
      ?: error("VertxRowMetadataHack failed: unexpected Vertx desc type")
    return rowDesc.columnDescriptor()
  }
}
