rootProject.name = "wasmo"

includeBuild("wasmo-build")
includeBuild("support/dom-tester/gradle-plugin")

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

include(":apps:journal:admin-web-app")
include(":apps:journal:api")
include(":apps:journal:db")
include(":apps:journal:wasmo-app")
include(":apps:samples")
include(":identifiers")
include(":os:api")
include(":os:catalog")
include(":os:cli")
include(":os:client:app")
include(":os:client:app-development")
include(":os:client:app-production")
include(":os:client:app-staging")
include(":os:client:compose")
include(":os:client:framework")
include(":os:client:passkeys:api")
include(":os:client:passkeys:real")
include(":os:client:smartphoneframe")
include(":os:framework")
include(":os:json")
include(":os:logging")
include(":os:routes")
include(":os:server:accounts:api")
include(":os:server:accounts:real")
include(":os:server:calls:api")
include(":os:server:calls:real")
include(":os:server:computers:api")
include(":os:server:computers:real")
include(":os:server:db")
include(":os:server:deployment")
include(":os:server:downloader:real")
include(":os:server:emails")
include(":os:server:events:api")
include(":os:server:events:logging")
include(":os:server:identifiers")
include(":os:server:installedapps:api")
include(":os:server:installedapps:real")
include(":os:server:jobs:api")
include(":os:server:jobs:memory")
include(":os:server:ktor")
include(":os:server:objectstore:api")
include(":os:server:objectstore:fs")
include(":os:server:objectstore:s3")
include(":os:server:okhttpclient")
include(":os:server:passkeys:api")
include(":os:server:passkeys:real")
include(":os:server:payments:api")
include(":os:server:payments:stripe")
include(":os:server:sendemail:api")
include(":os:server:sendemail:postmark")
include(":os:server:server-development")
include(":os:server:server-production")
include(":os:server:server-staging")
include(":os:server:sql:api")
include(":os:server:sql:jdbc")
include(":os:server:sql:r2dbc")
include(":os:server:testing")
include(":os:server:vault")
include(":os:server:wasm:api")
include(":os:server:wasm:jvm")
include(":os:server:wasm:real")
include(":os:server:website:api")
include(":os:server:website:real")
include(":platform:api")
include(":platform:packaging")
include(":platform:testing")
include(":support:dom-tester")
include(":support:issues")
include(":support:router")
include(":support:tokens")
include(":support:sqldelight-wasmo")
