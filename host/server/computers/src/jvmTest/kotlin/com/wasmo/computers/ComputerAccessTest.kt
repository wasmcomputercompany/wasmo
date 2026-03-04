package com.wasmo.computers

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.wasmo.api.ComputerSlug
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.ComputerListRoute
import com.wasmo.framework.UnauthorizedException
import com.wasmo.testing.WasmoServiceTester
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ComputerAccessTest {
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
  fun clientsCanSeeTheirOwnComputers() {
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
  fun clientsCannotSeeOtherUsersComputers() {
    val clientA = tester.newClient()
    val computerSlug = ComputerSlug("jesse99")
    clientA.createComputer(computerSlug)

    val clientB = tester.newClient()
    val computerListPage = clientB.call().hostPage(ComputerListRoute)
    assertThat(computerListPage.computerListSnapshot?.items).isNotNull().isEmpty()

    assertFailsWith<UnauthorizedException> {
      clientB.call().hostPage(ComputerHomeRoute(computerSlug))
    }
  }
}
