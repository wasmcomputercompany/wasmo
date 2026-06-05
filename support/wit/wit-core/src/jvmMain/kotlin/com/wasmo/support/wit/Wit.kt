package com.wasmo.support.wit

import okio.Path

/**
 * A collection of `.wit` files from a single file system directory.
 */
data class WitPackage(
  val packageDocumentation: Documentation? = null,
  val packageName: PackageName? = null,
  val files: Map<Path, WitFile>,
)

/**
 * Declarations may be [TopLevelUse], [Interface], [Package], and [World].
 */
data class WitFile(
  val packageDocumentation: Documentation? = null,
  val packageName: PackageName? = null,
  val declarations: List<Declaration>,
)
