package com.wasmo.sql

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import wasmo.sql.SqlService

@BindingContainer
abstract class SqlServiceBindings {
  @Binds
  abstract fun bindSqlDatabaseProvisioner(real: RealSqlDatabaseProvisioner): SqlDatabaseProvisioner

  @Binds
  abstract fun bindSqlService(real: RealSqlService): SqlService
}
