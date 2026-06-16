package com.wasmo.ktor

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class KtorBindings {
  @Binds
  internal abstract fun bindActionRouter(real: RealActionRouter): ActionRouter
}
