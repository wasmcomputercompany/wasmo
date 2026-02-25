plugins {
  alias(libs.plugins.kotlin.multiplatform)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.kotlinx.html)
        implementation(libs.kotlinx.io)
        implementation(libs.kotlinx.io.okio)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.server.call.logging)
        implementation(libs.ktor.server.config.yaml)
        implementation(libs.ktor.server.core)
        implementation(libs.ktor.server.default.headers)
        implementation(libs.ktor.server.host.common)
        implementation(libs.ktor.server.html.builder)
        implementation(libs.ktor.server.netty)
        implementation(libs.ktor.server.websockets)
        implementation(libs.logback.classic)
        implementation(libs.okhttp)
        implementation(libs.okio)
        implementation(libs.stripe)
        implementation(project(":client:app"))
        implementation(project(":common:api"))
        implementation(project(":common:catalog"))
        implementation(project(":common:framework"))
        implementation(project(":common:routes"))
        implementation(project(":common:tokens"))
        implementation(project(":platform:api"))
        implementation(project(":server:accounts:api"))
        implementation(project(":server:accounts:real"))
        implementation(project(":server:computers"))
        implementation(project(":server:db"))
        implementation(project(":server:deployment"))
        implementation(project(":server:downloader"))
        implementation(project(":server:objectstore:api"))
        implementation(project(":server:okhttpclient"))
        implementation(project(":server:passkeys:api"))
        implementation(project(":server:passkeys:real"))
        implementation(project(":server:payments:api"))
        implementation(project(":server:payments:stripe"))
        implementation(project(":server:sendemail:api"))
        implementation(project(":server:sendemail:postmark"))
        implementation(project(":server:website:api"))
        implementation(project(":server:website:real"))
      }
    }
  }
}
