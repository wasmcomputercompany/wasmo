package com.wasmo.support.wit

class WorldFlattener(
  private val index: SymbolIndex,
) {
  fun flatten(
    world: World,
    inPackageName: PackageName,
  ): World {
    val seed = IncludedWorld(
      path = UsePath(
        packageName = inPackageName,
        name = world.name,
      ),
      world = world,
    )

    val set = LinkedHashSet<IncludedWorld>()
    seed.collectIncludesRecursively(set)

    return world.copy(
      declarations = set.flatMap {
        it.world.declarations.filter { it !is Include }
      },
    )
  }

  private fun IncludedWorld.collectIncludesRecursively(
    set: MutableSet<IncludedWorld>,
  ) {
    if (!set.add(this)) return // Duplicate.

    for (include in world.declarations.filterIsInstance<Include>()) {
      val path = include.path.copy(
        packageName = include.path.packageName ?: path.packageName,
      )
      val world = index.getWorldOrNull(path)
      check(world != null) {
        "unable to find world $path included by ${this.path}"
      }

      IncludedWorld(
        path = path,
        world = world,
      ).collectIncludesRecursively(
        set = set,
      )
    }
  }

  private data class IncludedWorld(
    val path: UsePath,
    val world: World,
  )
}
