rootProject.name = "wasmo"

includeBuild("wasmo-build")
includeBuild("dom-tester-gradle-plugin")

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
include(":client:framework")
include(":client:passkeys:api")
include(":client:passkeys:real")
include(":common:api")
include(":common:catalog")
include(":common:framework")
include(":common:json")
include(":common:logging")
include(":common:routes")
include(":common:testing")
include(":common:tokens")
include(":dom-tester")
include(":platform:api")
include(":platform:testing")
include(":server:accounts:api")
include(":server:accounts:real")
include(":server:computers")
include(":server:db")
include(":server:deployment")
include(":server:downloader")
include(":server:emails")
include(":server:identifiers")
include(":server:ktor")
include(":server:objectstore:api")
include(":server:objectstore:fs")
include(":server:objectstore:s3")
include(":server:okhttpclient")
include(":server:passkeys:api")
include(":server:passkeys:real")
include(":server:payments:api")
include(":server:payments:stripe")
include(":server:sendemail:api")
include(":server:sendemail:postmark")
include(":server:server-development")
include(":server:server-production")
include(":server:server-staging")
include(":server:testing")
include(":server:vault")
include(":server:wasm")
include(":server:website:api")
include(":server:website:real")
