package com.wasmo.computers

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds

@BindingContainer
interface ComputerBindings {

  @Binds
  fun bindComputerStore(real: RealComputerStore): ComputerStore
}
