package com.wasmo.api.routes

import com.wasmo.framework.Url

interface RouteCodec {
  fun decode(url: Url): Route
  fun encode(route: Route): Url

  interface Factory {
    fun create(routingContext: RoutingContext): RouteCodec
  }
}
