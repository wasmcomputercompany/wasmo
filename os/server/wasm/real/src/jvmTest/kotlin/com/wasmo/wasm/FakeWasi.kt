package com.wasmo.wasm

import okio.Buffer

class FakeWasi : Wasi {
  val stderr = Buffer()
  val stdout = Buffer()

  override fun write(fd: Int, buffer: Buffer): Int {
    when (fd) {
      STDOUT -> {
        stdout.write(buffer, buffer.size)
        return Errno.success.ordinal
      }
      STDERR -> {
        stderr.write(buffer, buffer.size)
        return Errno.success.ordinal
      }
      else -> return Errno.badf.ordinal
    }
  }
}
