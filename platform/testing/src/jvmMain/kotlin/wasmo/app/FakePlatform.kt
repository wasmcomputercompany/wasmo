package wasmo.app

import com.wasmo.downloader.RealDownloader
import wasmo.http.FakeHttpService
import wasmo.jobqueue.FakeJobQueue
import wasmo.objectstore.FakeObjectStore
import wasmo.sql.SqlService
import wasmo.time.FakeClock

class FakePlatform(
  override val sqlService: SqlService,
) : Platform {
  override val clock = FakeClock()
  override val httpService = FakeHttpService()
  override val objectStore = FakeObjectStore()
  override val downloader = RealDownloader(httpService, objectStore)
  override val jobQueue = FakeJobQueue()
}
