package com.wasmo.support.wit.kotlin.generator

import com.wasmo.support.wit.Include
import com.wasmo.support.wit.PackageName
import com.wasmo.support.wit.SymbolIndex
import com.wasmo.support.wit.World

class WorldFlattener(
  private val index: SymbolIndex,
) {
  fun flatten(
    packageName: PackageName?,
    world: World,
  ) : World {

    for (declaration in world.declarations) {

      if (declaration !is Include) continue


    }

    return world
  }
}
