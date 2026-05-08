package com.wasmo.compose

import org.jetbrains.compose.web.attributes.AttrsScope

fun AttrsScope<*>.forceDark() {
  attr("data-theme", "dark")
}
