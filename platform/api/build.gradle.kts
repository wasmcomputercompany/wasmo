plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.brevity)
  id("wasmo-build")
}

wasmoBuild {
  library(
    jvm = true,
    wasm = true,
    publish = true,
  )
}

brevity {
  ociPackages.addAll(
    "wasi:cli@0.2.0",
    "wasi:clocks@0.2.0",
    "wasi:filesystem@0.2.0",
    "wasi:http@0.2.0",
    "wasi:io@0.2.0",
    "wasi:random@0.2.0",
    "wasi:sockets@0.2.0",
  )
  customTypeMappings.put("wasmo:uuid/types.uuid@0.1.0", "kotlin.uuid.Uuid")
  customTypeMappings.put("wasmo:object-store/types.key@0.1.0", "wasmo.objectstore.Key")
  customTypeMappings.put("wasi:clocks/wall-clock.datetime@0.2.0", "kotlin.time.Instant")
  worlds.add("wasmo:platform/wasmo")
}

kotlin {
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.brevity)
        implementation(libs.jetbrains.annotations)
        implementation(libs.kotlinx.serialization.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
      }
    }
    val jvmMain by getting {
      dependencies {
        implementation(libs.okhttp)
        implementation(libs.tomlkt)
      }
    }
  }
}
