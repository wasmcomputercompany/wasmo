package com.wasmo.gradle

interface WasmoBuildExtension {
  fun libraryJs()
  fun libraryJvm()
  fun libraryJvmJs()
  fun consumeJsResources()
  fun applicationJs(name: String, artifactTaskName: String)
}
