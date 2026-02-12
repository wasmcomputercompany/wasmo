rootProject.name = "wasmo"

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

include(":apps:hello:api")
include(":apps:hello:db")
include(":apps:hello:server")
include(":client:app")
include(":client:app-development")
include(":client:app-production")
include(":client:app-staging")
include(":common:api")
include(":common:framework")
include(":common:json")
include(":common:logging")
include(":common:testing")
include(":common:tokens")
include(":platform:api")
include(":platform:okhttpclient")
include(":platform:testing")
include(":server:actions")
include(":server:db")
include(":server:identifiers")
include(":server:ktor")
include(":server:objectstore")
include(":server:objectstore:fs")
include(":server:objectstore:s3")
include(":server:server-development")
include(":server:server-production")
include(":server:server-staging")
include(":server:testing")
include(":server:vault")
include(":server:wasm")
