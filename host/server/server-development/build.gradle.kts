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
  add("jsResources", project(":host:client:app-development"))
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":apps:samples"))
  implementation(project(":host:api"))
  implementation(project(":host:catalog"))
  implementation(project(":host:server:accounts:real"))
  implementation(project(":host:server:computers:api"))
  implementation(project(":host:server:computers:real"))
  implementation(project(":host:server:deployment"))
  implementation(project(":host:server:ktor"))
  implementation(project(":host:server:objectstore:api"))
  implementation(project(":host:server:payments:stripe"))
  implementation(project(":host:server:sendemail:postmark"))
  implementation(project(":host:server:sql:api"))
  implementation(project(":host:server:website:real"))
  implementation(project(":platform:api"))
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
