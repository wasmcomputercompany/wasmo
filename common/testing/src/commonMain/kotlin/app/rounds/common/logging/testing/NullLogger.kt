package app.rounds.common.logging.testing

import app.rounds.common.logging.Logger

object NullLogger : Logger {
  override fun info(message: String, throwable: Throwable?) {
  }
}
