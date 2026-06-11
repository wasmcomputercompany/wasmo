package dev.wasmo.brevity.ir

import dev.wasmo.brevity.Identifier
import dev.wasmo.brevity.PackageName

/**
 * The name of either an interface or a world.
 */
data class IrParentName(
  val packageName: PackageName,
  val name: Identifier,
)
