package com.wasmo.wasm

import okio.Buffer

val STDOUT = 1
val STDERR = 2

interface Wasi {
  fun write(fd: Int, buffer: Buffer): Errno
}
