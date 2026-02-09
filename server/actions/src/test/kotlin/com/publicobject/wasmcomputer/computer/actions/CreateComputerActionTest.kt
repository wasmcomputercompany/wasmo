package com.publicobject.wasmcomputer.computer.actions

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.publicobject.wasmcomputer.api.CreateComputerRequest
import com.publicobject.wasmcomputer.api.CreateComputerResponse
import com.publicobject.wasmcomputer.framework.Response
import com.publicobject.wasmcomputer.testing.WasmComputerTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateComputerActionTest {
  lateinit var tester: WasmComputerTester

  @BeforeTest
  fun setUp() {
    tester = WasmComputerTester.start()
  }

  @AfterTest
  fun tearDown() {
    tester.close()
  }

  @Test
  fun happyPath() {
    val response = tester.createComputerAction().createComputer(
      request = CreateComputerRequest(
        slug = "computer-one",
      ),
    )
    assertThat(response).isEqualTo(
      Response(
        body = CreateComputerResponse(
          url = "/computer/computer-one",
        ),
      ),
    )
  }
}
