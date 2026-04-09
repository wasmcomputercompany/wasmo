package com.wasmo.support.absurd

import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class SampleTest {
  val tester = AbsurdTester.create()

  @Test
  fun sample() = runTest {
    tester.clearSchema()
  }
}
