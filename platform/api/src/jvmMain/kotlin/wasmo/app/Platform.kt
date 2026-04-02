package wasmo.app

import kotlin.time.Clock
import wasmo.downloader.Downloader
import wasmo.http.HttpService
import wasmo.jobs.JobQueue
import wasmo.objectstore.ObjectStore
import wasmo.sql.SqlService

interface Platform {
  val clock: Clock
  val httpService: HttpService
  val objectStore: ObjectStore
  val downloader: Downloader
  val sqlService: SqlService
  val jobQueue: JobQueue
}
