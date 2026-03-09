package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.wasmo.api.ComputerSlug
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.framework.UnauthorizedUserException
import com.wasmo.testing.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class ComputerAccessTest {
  @InterceptTest
  val tester = ServiceTester()

  @Test
  fun clientsCanSeeTheirOwnComputers() = runTest {
    val clientA = tester.newClient()
    val computerSlug = ComputerSlug("jesse99")
    clientA.createComputer(computerSlug)

    val computerListPage = clientA.call().hostPage(ComputerListRoute)
    assertThat(computerListPage.computerListSnapshot?.items)
      .isNotNull()
      .isNotEmpty()

    val computerHomePage = clientA.call().hostPage(ComputerHomeRoute(computerSlug))
    assertThat(computerHomePage.computerSnapshot?.slug).isEqualTo(computerSlug)
  }

  @Test
  fun clientsCannotSeeOtherUsersComputers() = runTest {
    val clientA = tester.newClient()
    val computerSlug = ComputerSlug("jesse99")
    clientA.createComputer(computerSlug)

    val clientB = tester.newClient()
    val computerListPage = clientB.call().hostPage(ComputerListRoute)
    assertThat(computerListPage.computerListSnapshot?.items).isNotNull().isEmpty()

    assertFailsWith<UnauthorizedUserException> {
      clientB.call().hostPage(ComputerHomeRoute(computerSlug))
    }
  }
}
