package wasmo.app

import wasmo.http.HttpService

interface WasmoApp {
  val httpService: HttpService?

  /**
   * Invoked after an app is first installed, and after each version update.
   *
   * @param oldVersion will be 0 if this is the first install of the app.
   */
  suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  )

  interface Factory {
    suspend fun create(platform: Platform): WasmoApp
  }
}
