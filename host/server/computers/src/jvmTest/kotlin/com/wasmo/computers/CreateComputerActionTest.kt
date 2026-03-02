package com.wasmo.computers

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.wasmo.api.ComputerListItem
import com.wasmo.api.ComputerSlug
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
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
    val client = tester.newClient()
    val computerSlug = ComputerSlug("jesse99")
    val createComputerResponse = client.createComputerAction().create(
      request = CreateComputerRequest(
        computerSpecToken = "computerspectoken00000001",
        slug = computerSlug,
      ),
    )
    val checkoutSessionId = client.paymentsService.completePayment(
      createComputerResponse.body.checkoutSessionClientSecret,
    )
    val afterCheckoutResponse = client.afterCheckoutAction().get(checkoutSessionId)

    assertThat(afterCheckoutResponse.header("Location")).isEqualTo(
      "https://jesse99.wasmo.com/",
    )

    val computerListPage = client.hostPage(ComputerListRoute)
    assertThat(computerListPage.computerListSnapshot?.items)
      .isNotNull()
      .containsExactly(ComputerListItem(computerSlug))

    val computerHostPage = client.hostPage(ComputerHomeRoute(computerSlug))
    assertThat(computerHostPage.computerSnapshot?.slug).isEqualTo(computerSlug)
  }
}
