package com.wasmo.client.app

import org.w3c.dom.Element

external class ResizeObserver {
  constructor(block: () -> Unit)

  fun observe(element: Element)
  fun unobserve(element: Element)
}
