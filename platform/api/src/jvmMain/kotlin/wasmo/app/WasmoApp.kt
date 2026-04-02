package wasmo.app

import wasmo.http.HttpService
import wasmo.jobs.JobHandler

abstract class WasmoApp {
  open val httpService: HttpService?
    get() = null
  open val jobHandler: JobHandler?
    get() = null

  /**
   * Invoked after an app is first installed, and after each version update.
   *
   * @param oldVersion will be 0 if this is the first install of the app.
   */
  open suspend fun afterInstall(
    oldVersion: Long,
    newVersion: Long,
  ) {
  }

  fun interface Factory {
    suspend fun create(platform: Platform): WasmoApp
  }
}
