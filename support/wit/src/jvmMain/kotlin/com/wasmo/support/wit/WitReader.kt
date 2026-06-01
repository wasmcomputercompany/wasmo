package com.wasmo.support.wit

class WitReader private constructor(
  private val source: WitStructureReader,
) {
  constructor(string: String) : this(WitStructureReader(string))

  fun read(): WitFile {
    error("TODO")
  }
}
