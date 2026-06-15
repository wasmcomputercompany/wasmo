package com.wasmo.wasm

import wit.wasmo.testing.WasmoTesting

val actuallyInitialize = run {
  guest = object : WasmoTesting.Guest {
    override fun sum(a: Long, b: Long): Long {
      return a + b
    }
  }

  concatenator = object : Concatenator {
    override fun concatenate(a: String, b: String) = a + b
  }
}
