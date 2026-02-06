package app.rounds.testing

import app.rounds.app.db.RoundsDbService
import app.rounds.common.testing.FakeClock
import java.io.Closeable

/**
 * Create instances with [RoundsTester.start]
 */
class RoundsTester private constructor(
  val service: RoundsDbService,
) : Closeable by service {
  val clock = FakeClock()

  companion object {
    fun start(): RoundsTester {
      val service = RoundsDbService.start(
        databaseName = "roundscomputer_test",
        user = "postgres",
        password = "password",
        hostname = "localhost",
        ssl = false,
      )
      service.clearSchema()
      service.migrate()
      return RoundsTester(service)
    }
  }
}
