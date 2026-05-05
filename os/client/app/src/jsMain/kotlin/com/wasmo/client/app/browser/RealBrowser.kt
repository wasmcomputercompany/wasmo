package com.wasmo.client.app.browser

import com.wasmo.client.identifiers.ClientAppScope
import com.wasmo.framework.Url
import com.wasmo.framework.encodePathAndQuery
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.browser.window
import org.w3c.dom.PopStateEvent

@Inject
@SingleIn(ClientAppScope::class)
class RealBrowser : Browser {
  override var locationHref: String by window.location::href
  override var onpopstate: ((PopStateEvent) -> dynamic)? by window::onpopstate

  override fun back() {
    window.history.back()
  }

  override fun pushState(data: Any?, title: String, url: Url?) {
    window.history.pushState(data, title, url?.encodePathAndQuery())
  }

  override fun replaceState(data: Any?, title: String, url: Url?) {
    window.history.replaceState(data, title, url?.encodePathAndQuery())
  }
}
