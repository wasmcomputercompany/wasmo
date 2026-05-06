plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  id("wasmo-build")
}

wasmoBuild {
  consumeJsResources("static/assets")
}

application {
  mainClass = "com.wasmo.distributions.homelab.HomelabWasmoOs"
}

dependencies {
  add("jsResources", projects.os.client.appHomelab)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(projects.apps.journal.wasmoApp)
  implementation(projects.apps.samples)
  implementation(projects.os.api)
  implementation(projects.os.catalog)
  implementation(projects.os.server.accounts.real)
  implementation(projects.os.server.computers.api)
  implementation(projects.os.server.computers.real)
  implementation(projects.os.server.identifiers)
  implementation(projects.os.server.ktor)
  implementation(projects.os.server.objectstore.api)
  implementation(projects.os.server.payments.stripe)
  implementation(projects.os.server.sendemail.postmark)
  implementation(projects.os.server.sql.api)
  implementation(projects.os.server.website.real)
  implementation(projects.platform.api)
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
