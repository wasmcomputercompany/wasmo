package com.wasmo.postgresqloperator

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class ExternalPostgresqlOperatorBindings {

  @Binds
  internal abstract fun bindPostgresqlOperator(real: ExternalPostgresqlOperator): PostgresqlOperator
}
