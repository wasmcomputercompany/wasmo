package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.framework.UnauthorizedUserException
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class ComputerAccessTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun clientsCanSeeTheirOwnComputers() = runTest {
    val clientA = tester.newClient()
    val computer = clientA.createComputer()

    val computerListPage = clientA.call().osPage(HomeRoute)
    assertThat(computerListPage.computerListSnapshot?.items)
      .isNotNull()
      .isNotEmpty()

    val computerHomePage = computer.homePage()
    assertThat(computerHomePage.computerSnapshot?.slug).isEqualTo(computer.slug)
  }

  @Test
  fun clientsCannotSeeOtherUsersComputers() = runTest {
    val clientA = tester.newClient()
    val computer = clientA.createComputer()

    val clientB = tester.newClient()
    val computerListPage = clientB.call().osPage(HomeRoute)
    assertThat(computerListPage.computerListSnapshot?.items).isNotNull().isEmpty()

    assertFailsWith<UnauthorizedUserException> {
      clientB.call().osPage(ComputerHomeRoute(computer.slug))
    }
  }
}
