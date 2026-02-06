package app.rounds.common.logging

object ConsoleLogger : Logger {
  override fun info(message: String, throwable: Throwable?) {
    if (throwable != null) {
      console.log(message, throwable)
    } else {
      console.log(message)
    }
  }
}
