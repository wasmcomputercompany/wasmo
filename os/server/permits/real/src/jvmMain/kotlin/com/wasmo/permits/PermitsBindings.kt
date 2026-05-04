package com.wasmo.permits

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class PermitsBindings {
  @Binds
  internal abstract fun bindPermitService(real: RealPermitService): PermitService
}
