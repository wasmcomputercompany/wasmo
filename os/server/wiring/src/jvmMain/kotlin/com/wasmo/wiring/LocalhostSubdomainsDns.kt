package com.wasmo.wiring

import java.net.InetAddress
import okhttp3.Dns

/**
 * An OkHttp DNS that routes `app-computer.wasmo.localhost` to the same address as `localhost`.
 */
class LocalhostSubdomainsDns(
  val delegate: Dns,
) : Dns {
  override fun lookup(hostname: String): List<InetAddress> {
    return when {
      hostname.endsWith(".localhost") -> delegate.lookup("localhost")
      else -> delegate.lookup(hostname)
    }
  }
}
