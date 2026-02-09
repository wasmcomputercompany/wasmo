package com.wasmo.computers

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.framework.Response
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class CreateComputerActionTest {
  lateinit var tester: WasmoServiceTester

  @BeforeTest
  fun setUp() {
    tester = WasmoServiceTester.start()
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
