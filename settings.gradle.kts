rootProject.name = "wasmo"

dependencyResolutionManagement {
  repositories {
    mavenCentral()
  }
}

include(":apps:hello:api")
include(":apps:hello:db")
include(":apps:hello:server")
include(":client:app")
include(":client:development-app")
include(":common:api")
include(":common:framework")
include(":common:json")
include(":common:logging")
include(":common:testing")
include(":common:tokens")
include(":platform:api")
include(":platform:filesystemobjectstore")
include(":platform:testing")
include(":server:actions")
include(":server:db")
include(":server:identifiers")
include(":server:ktor")
include(":server:server-development")
include(":server:testing")
include(":server:wasm")
