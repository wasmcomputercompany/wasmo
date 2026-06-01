package com.wasmo.wasm

import okio.Buffer

class FakeWasi : Wasi {
  val stderr = Buffer()
  val stdout = Buffer()

  override fun write(fd: Int, buffer: Buffer): Errno {
    when (fd) {
      STDOUT -> {
        stdout.write(buffer, buffer.size)
        return Errno.success
      }
      STDERR -> {
        stderr.write(buffer, buffer.size)
        return Errno.success
      }
      else -> return Errno.badf
    }
  }
}
