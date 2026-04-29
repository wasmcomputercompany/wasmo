package wasmo.jobs

import okio.ByteString

interface JobHandler {
  suspend fun handle(job: ByteString)

  interface Factory {
    fun get(queueName: String): JobHandler
  }
}
