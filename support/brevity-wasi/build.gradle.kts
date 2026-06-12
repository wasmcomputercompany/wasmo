plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
  id("brevity")
}

wasmoBuild {
  libraryJvmWasm()
}

brevity {
  generateKotlin {
    inputWitPackageDirectories.from(
      File(project.rootDir, "submodules/WASI/proposals/cli/wit"),
      File(project.rootDir, "submodules/WASI/proposals/clocks/wit"),
      File(project.rootDir, "submodules/WASI/proposals/filesystem/wit"),
      File(project.rootDir, "submodules/WASI/proposals/http/wit"),
      File(project.rootDir, "submodules/WASI/proposals/io/wit"),
      File(project.rootDir, "submodules/WASI/proposals/random/wit"),
      File(project.rootDir, "submodules/WASI/proposals/sockets/wit"),
    )
  }
}
