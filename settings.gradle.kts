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
include(":apps:samples")
include(":dom-tester")
include(":host:api")
include(":host:catalog")
include(":host:client:app")
include(":host:client:app-development")
include(":host:client:app-production")
include(":host:client:app-staging")
include(":host:client:compose")
include(":host:client:framework")
include(":host:client:launcher")
include(":host:client:passkeys:api")
include(":host:client:passkeys:real")
include(":host:client:smartphoneframe")
include(":host:framework")
include(":host:json")
include(":host:logging")
include(":host:routes")
include(":host:server:accounts:api")
include(":host:server:accounts:real")
include(":host:server:calls:api")
include(":host:server:calls:real")
include(":host:server:computers:api")
include(":host:server:computers:real")
include(":host:server:db")
include(":host:server:deployment")
include(":host:server:emails")
include(":host:server:events:api")
include(":host:server:events:logging")
include(":host:server:identifiers")
include(":host:server:installedapps:api")
include(":host:server:installedapps:real")
include(":host:server:jobs:api")
include(":host:server:jobs:memory")
include(":host:server:ktor")
include(":host:server:objectstore:api")
include(":host:server:objectstore:fs")
include(":host:server:objectstore:s3")
include(":host:server:okhttpclient")
include(":host:server:passkeys:api")
include(":host:server:passkeys:real")
include(":host:server:payments:api")
include(":host:server:payments:stripe")
include(":host:server:sendemail:api")
include(":host:server:sendemail:postmark")
include(":host:server:server-development")
include(":host:server:server-production")
include(":host:server:server-staging")
include(":host:server:testing")
include(":host:server:vault")
include(":host:server:wasm:api")
include(":host:server:wasm:real")
include(":host:server:website:api")
include(":host:server:website:real")
include(":host:tokens")
include(":identifiers")
include(":platform:api")
include(":platform:issues")
include(":platform:packaging")
include(":platform:testing")
