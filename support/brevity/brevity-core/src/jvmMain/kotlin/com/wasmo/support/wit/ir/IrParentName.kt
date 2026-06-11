package com.wasmo.support.wit.ir

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName

/**
 * The name of either an interface or a world.
 */
data class IrParentName(
  val packageName: PackageName,
  val name: Identifier,
)
