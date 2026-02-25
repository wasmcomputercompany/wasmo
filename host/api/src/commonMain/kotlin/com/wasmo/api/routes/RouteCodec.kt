package com.wasmo.api.routes

interface RouteCodec {
  fun decode(url: Url): Route
  fun encode(route: Route): Url
}
