package com.wasmo.gradle

import org.gradle.api.tasks.TaskProvider

interface WasmoBuildExtension {
  fun libraryJs() {
    library(js = true)
  }

  fun libraryJvmJs() {
    library(jvm = true, js = true)
  }

  fun libraryJvmWasm() {
    library(jvm = true, wasm = true)
  }

  fun libraryJvm() {
    library(jvm = true)
  }

  fun library(
    jvm: Boolean = false,
    js: Boolean = false,
    wasm: Boolean = false,
    publish: Boolean = false,
  )

  fun consumeJsResources(path: String)
  fun applicationJs(name: String, artifactTaskName: String)
  fun createWasmoFileTask(slug: String): TaskProvider<WasmoFileTask>
}
