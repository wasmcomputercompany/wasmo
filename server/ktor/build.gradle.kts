plugins {
  alias(libs.plugins.kotlin.jvm)
}

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
  implementation(project(":common:api"))
  implementation(project(":common:framework"))
  implementation(project(":common:tokens"))
  implementation(project(":platform:api"))
  implementation(project(":platform:okhttpclient"))
  implementation(project(":server:actions"))
  implementation(project(":server:db"))
  implementation(project(":server:objectstore"))
}
