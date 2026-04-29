package com.wasmo.gradle

import org.gradle.api.tasks.TaskProvider

interface WasmoBuildExtension {
  fun libraryJs()
  fun libraryJvm()
  fun libraryJvmJs()
  fun libraryJvmWasm()
  fun consumeJsResources(path: String)
  fun applicationJs(name: String, artifactTaskName: String)
  fun createWasmoFileTask(slug: String): TaskProvider<WasmoFileTask>
}
