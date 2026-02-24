package com.wasmo.client.app.browser

import com.wasmo.api.routes.Url
import org.w3c.dom.PopStateEvent

interface Browser {
  var locationHref: String
  var onpopstate: ((PopStateEvent) -> dynamic)?
  fun back()
  fun pushState(data: Any?, title: String, url: Url?)
  fun replaceState(data: Any?, title: String, url: Url?)
}
