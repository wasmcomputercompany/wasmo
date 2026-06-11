package com.wasmo.support.wit.ir

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName

data class IrParentName(
  val packageName: PackageName,
  val name: Identifier,
)
