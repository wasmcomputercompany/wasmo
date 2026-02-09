package com.wasmo.framework

import kotlinx.html.HEAD
import kotlinx.html.script
import kotlinx.html.unsafe

fun MapPageData.write(head: HEAD) {
  head.script {
    unsafe {
      raw("""document.pageData=${json.encodeToString(map)};""")
    }
  }
}
