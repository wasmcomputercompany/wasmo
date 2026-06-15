package dev.wasmo.brevity

import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store

interface World<H, G> {
  val host: H
  val guest: G

  fun initExports(instance: Instance)
  fun initImports(store: Store)
}
