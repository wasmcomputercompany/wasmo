@file:OptIn(ExperimentalStdlibApi::class)

package com.wasmo.ktor

import com.wasmo.http.RealHttpClient
import com.wasmo.api.CreateComputerRequest
import com.wasmo.api.CreateComputerResponse
import com.wasmo.app.db.WasmoDbService
import com.wasmo.apps.ObjectStoreKeyFactory
import com.wasmo.computers.RealComputerStore
import com.wasmo.framework.HttpException
import com.wasmo.home.HomePage
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.objectstore.ObjectStoreFactory
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlin.time.Clock
import okhttp3.HttpUrl
import okhttp3.OkHttpClient

class WasmoService(
  val postgresDatabaseHostname: String,
  val postgresDatabaseName: String,
  val postgresDatabaseUser: String,
  val postgresDatabasePassword: String,
  val baseUrl: HttpUrl,
  val objectStoreAddress: ObjectStoreAddress,
) {
  private val createComputerRequestAdapter = CreateComputerRequest.serializer()
  private val createComputerResponseAdapter = CreateComputerResponse.serializer()

  fun start(args: Array<String>) {
    val server = EngineMain.createServer(args)

    val clock = Clock.System
    val service = WasmoDbService.start(
      hostname = postgresDatabaseHostname,
      databaseName = postgresDatabaseName,
      user = postgresDatabaseUser,
      password = postgresDatabasePassword,
      ssl = false,
    )
    val okHttpClient = OkHttpClient()
    val httpClient = RealHttpClient(
      callFactory = okHttpClient,
    )
    val objectStoreFactory = ObjectStoreFactory(
      clock,
      okHttpClient,
    )
    val rootObjectStore = objectStoreFactory.open(objectStoreAddress)
    val computerStore = RealComputerStore(
      baseUrl = baseUrl,
      clock = clock,
      rootObjectStore = rootObjectStore,
      httpClient = httpClient,
      objectStoreKeyFactory = ObjectStoreKeyFactory(),
      service = service,
    )
    val actionFactory = ActionFactory(
      computerStore = computerStore,
    )
    configureServer(
      application = server.application,
      actionFactory = actionFactory,
    )

    server.start(true)
  }

  fun configureServer(
    application: Application,
    actionFactory: ActionFactory,
  ) {
    application.install(CallLogging)
    application.routing {
      get("/") {
        val action = HomePage(baseUrl)
        val page = action.get()
        call.respond(page.response)
      }

      // Rest RPCs.
      post("/create-computer") {
        val action = actionFactory.createComputerAction()
        val response = try {
          action.createComputer(createComputerRequestAdapter.decode(call.request))
        } catch (e: HttpException) {
          application.log.info("call failed", e)
          call.respond(e.asResponse())
          return@post
        }
        call.respond(
          serializer = createComputerResponseAdapter,
          response = response,
        )
      }

      staticResources("/", "static")
    }
  }
}

