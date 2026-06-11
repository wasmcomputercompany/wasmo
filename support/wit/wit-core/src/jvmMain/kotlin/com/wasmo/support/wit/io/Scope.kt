package com.wasmo.support.wit.io

import com.wasmo.support.wit.Identifier
import com.wasmo.support.wit.PackageName

/**
 * A logical place to in a `.wit` declaration tree. Use this to resolve relative references to
 * types.
 *
 * Within a `use` or `include` declaration, the scope is where to search, which is different from
 * the enclosing scope. For example, the `pollable` symbol will be resolved in the `wasi:io@0.2.12`
 * package.
 *
 * ```wit
 * package wasi:clocks@0.2.12;
 *
 * interface monotonic-clock {
 *   use wasi:io/poll@0.2.12.{pollable};
 * }
 * ```
 */
data class Scope(
  val packageName: PackageName,
  val interfaceName: Identifier? = null,
) {
  fun copy(usePath: UsePath): Scope = copy(
    packageName = usePath.packageName ?: packageName,
    interfaceName = usePath.name,
  )

  override fun toString(): String {
    return when {
      interfaceName != null -> UsePath(packageName, interfaceName).toString()
      else -> packageName.toString()
    }
  }
}
