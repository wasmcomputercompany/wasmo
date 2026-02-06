rootProject.name = "wasmcomputer"

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

include(":client:app")
include(":client:development-app")
include(":common:api")
include(":common:framework")
include(":common:logging")
include(":common:testing")
include(":common:tokens")
include(":server:actions")
include(":server:db")
include(":server:identifiers")
include(":server:ktor")
include(":server:server-development")
include(":server:testing")
