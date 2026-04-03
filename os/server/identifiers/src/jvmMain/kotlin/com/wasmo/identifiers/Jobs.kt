package com.wasmo.identifiers

import kotlinx.serialization.KSerializer

interface Job {
  val handlerId: JobHandlerId<*>
}

interface JobHandlerId<J : Job> {
  val serializer: KSerializer<J>
}
