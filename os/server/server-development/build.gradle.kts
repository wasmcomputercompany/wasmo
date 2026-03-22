plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  id("wasmo-build")
}

wasmoBuild {
  consumeJsResources("static/assets")
}

application {
  mainClass = "com.wasmo.ktor.development.WasmoServerDevelopment"
}

dependencies {
  add("jsResources", project(":os:client:app-development"))
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":apps:samples"))
  implementation(project(":os:api"))
  implementation(project(":os:catalog"))
  implementation(project(":os:server:accounts:real"))
  implementation(project(":os:server:computers:api"))
  implementation(project(":os:server:computers:real"))
  implementation(project(":os:server:deployment"))
  implementation(project(":os:server:ktor"))
  implementation(project(":os:server:objectstore:api"))
  implementation(project(":os:server:payments:stripe"))
  implementation(project(":os:server:sendemail:postmark"))
  implementation(project(":os:server:sql:api"))
  implementation(project(":os:server:website:real"))
  implementation(project(":platform:api"))
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
