rootProject.name = "wasmo"

includeBuild("build-support")

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
include(":client:compose")
include(":common:api")
include(":common:framework")
include(":common:json")
include(":common:logging")
include(":common:testing")
include(":common:tokens")
include(":dom-tester")
include(":platform:api")
include(":platform:testing")
include(":server:accounts")
include(":server:accounts:api")
include(":server:computers")
include(":server:db")
include(":server:deployment")
include(":server:downloader")
include(":server:emails")
include(":server:identifiers")
include(":server:ktor")
include(":server:objectstore")
include(":server:objectstore:fs")
include(":server:objectstore:s3")
include(":server:okhttpclient")
include(":server:passkeys")
include(":server:passkeys:api")
include(":server:sendemail")
include(":server:sendemail:api")
include(":server:server-development")
include(":server:server-production")
include(":server:server-staging")
include(":server:testing")
include(":server:vault")
include(":server:wasm")
include(":server:website")
