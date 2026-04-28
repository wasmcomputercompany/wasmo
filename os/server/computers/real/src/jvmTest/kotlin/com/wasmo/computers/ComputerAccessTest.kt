package com.wasmo.computers

import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import com.wasmo.api.routes.ComputerHomeRoute
import com.wasmo.api.routes.HomeRoute
import com.wasmo.framework.ContentTypes
import com.wasmo.framework.NotFoundUserException
import com.wasmo.framework.Response
import com.wasmo.framework.UnauthorizedUserException
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.testing.apps.RecipesApp
import com.wasmo.testing.framework.ResponseBodySnapshot
import com.wasmo.testing.service.ServiceTester
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.encodeUtf8
import wasmo.objectstore.PutObjectRequest

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

  @Test
  fun accessIsScoped() = runTest {
    val app = tester.sampleApps.recipes.publishedApp
    tester.publishApp(app)

    val userA = tester.newClient()
    val userAComputer = userA.createComputer(ComputerSlug("apple"))
    val userAInstalledApp = userAComputer.installApp(app)

    val userARecipesApp = userAInstalledApp.load() as RecipesApp
    userARecipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www/pizza.txt",
        value = "Ham and Pineapple".encodeUtf8(),
        contentType = ContentTypes.TextPlain.toString(),
      ),
    )

    val userB = tester.newClient()
    val userBComputer = userB.createComputer(ComputerSlug("banana"))
    val userBInstalledApp = userBComputer.installApp(app)

    val userBRecipesApp = userBInstalledApp.load() as RecipesApp
    userBRecipesApp.platform.objectStore.put(
      PutObjectRequest(
        key = "www/pizza.txt",
        value = "Pepperoni".encodeUtf8(),
        contentType = ContentTypes.TextPlain.toString(),
      ),
    )

    assertThat(userA.call().callApp(url = userAInstalledApp.url.resolve("/pizza.txt")!!))
      .isEqualTo(
        Response(
          body = ResponseBodySnapshot("Ham and Pineapple"),
          contentType = ContentTypes.TextPlain,
        ),
      )
    assertFailsWith<NotFoundUserException> {
      userA.call().callApp(url = userBInstalledApp.url.resolve("/pizza.txt")!!)
    }

    assertThat(userB.call().callApp(url = userBInstalledApp.url.resolve("/pizza.txt")!!))
      .isEqualTo(
        Response(
          body = ResponseBodySnapshot("Pepperoni"),
          contentType = ContentTypes.TextPlain,
        ),
      )
    assertFailsWith<NotFoundUserException> {
      userB.call().callApp(url = userAInstalledApp.url.resolve("/pizza.txt")!!)
    }
  }
}
