package com.wasmo.ktor

import com.wasmo.common.logging.Logger
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class KtorBindings {
  @Binds
  internal abstract fun bindActionRouter(real: RealActionRouter): ActionRouter

  @Binds
  internal abstract fun bindLogger(real: KtorLogger): Logger
}
