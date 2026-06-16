package com.wasmo.wasm

import wit.wasmo.testing.Calculator
import wit.wasmo.testing.WasmoTesting
import wit.wasmo.testing.guest

/**
 * Note that [EagerInitialization] is necessary to trigger the side effect of initializing the
 * exported worlds.
 */
@OptIn(ExperimentalStdlibApi::class)
@EagerInitialization
val actuallyInitialize = run {
  WasmoTesting.guest = object : WasmoTesting.Guest {
    override val calculator = object : Calculator {
      override fun multiply(a: Long, b: Long) = a * b
    }

    override fun sum(a: Long, b: Long): Long {
      return a + b
    }
  }

  concatenator = object : Concatenator {
    override fun concatenate(a: String, b: String) = a + b
  }
}
