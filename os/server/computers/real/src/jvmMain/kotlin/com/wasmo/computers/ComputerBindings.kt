package com.wasmo.computers

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
abstract class ComputerBindings {

  @Binds
  abstract fun bindComputerStore(real: RealComputerStore): ComputerStore
}
