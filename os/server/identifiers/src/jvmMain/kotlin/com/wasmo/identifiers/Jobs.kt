package com.wasmo.identifiers

import kotlinx.serialization.KSerializer

interface HandlerId<J : Job> {
  val serializer: KSerializer<J>
}

interface Job {
  val handlerId: HandlerId<*>
}
