package com.wasmo.gradle

interface WasmoBuildExtension {
  fun libraryJs()
  fun libraryJvm()
  fun libraryJvmJs()
  fun consumeJsResources(path: String)
  fun applicationJs(name: String, artifactTaskName: String)
}
