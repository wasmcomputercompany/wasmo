package com.wasmo.support.wit

import okio.Buffer
import okio.BufferedSource

class WitReader(
  private val source: BufferedSource,
) {
  constructor(string: String) : this(Buffer().writeUtf8(string))

  fun read() : WitFile {
    error("TODO")
  }
}

