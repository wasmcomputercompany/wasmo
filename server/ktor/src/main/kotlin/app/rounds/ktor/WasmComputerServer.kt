@file:OptIn(ExperimentalStdlibApi::class)

package app.rounds.ktor

import app.rounds.account.actions.CreateComputerAction
import app.rounds.account.api.CreateComputerRequest
import app.rounds.account.api.CreateComputerResponse
import app.rounds.app.db.RoundsDbService
import app.rounds.framework.HttpException
import app.rounds.home.actions.HomePage
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

class WasmComputerServer(
  val postgresDatabaseHostname: String,
  val postgresDatabaseName: String,
  val postgresDatabaseUser: String,
  val postgresDatabasePassword: String,
) {
  private val createComputerRequestAdapter = CreateComputerRequest.serializer()
  private val createComputerResponseAdapter = CreateComputerResponse.serializer()

  fun start(
    withWebSockets: Boolean = false,
    args: Array<String>,
  ) {
    val server = EngineMain.createServer(args)

    val service = RoundsDbService.start(
      hostname = postgresDatabaseHostname,
      databaseName = postgresDatabaseName,
      user = postgresDatabaseUser,
      password = postgresDatabasePassword,
      ssl = false,
    )

    val clock = Clock.System

    configureRounds(
      application = server.application,
    )

    server.start(true)
  }

  fun configureRounds(
    application: Application,
  ) {
    application.install(CallLogging)
    application.routing {
      get("/") {
        val action = HomePage()
        val page = action.get()
        call.respond(page.response)
      }

      // Rest RPCs.
      post("/create-computer") {
        val action = CreateComputerAction()
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

