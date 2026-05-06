plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
  alias(libs.plugins.metro)
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
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(projects.apps.journal.wasmoApp)
  implementation(projects.apps.samples)
  implementation(projects.identifiers)
  implementation(projects.os.api)
  implementation(projects.os.catalog)
  implementation(projects.os.framework)
  implementation(projects.os.routes)
  implementation(projects.os.server.accounts.api)
  implementation(projects.os.server.accounts.real)
  implementation(projects.os.server.calls.api)
  implementation(projects.os.server.calls.real)
  implementation(projects.os.server.calls.wiring)
  implementation(projects.os.server.computers.api)
  implementation(projects.os.server.computers.real)
  implementation(projects.os.server.db)
  implementation(projects.os.server.emails.real)
  implementation(projects.os.server.events.api)
  implementation(projects.os.server.events.logging)
  implementation(projects.os.server.identifiers)
  implementation(projects.os.server.installedapps.real)
  implementation(projects.os.server.jobs.absurd)
  implementation(projects.os.server.jobs.api)
  implementation(projects.os.server.ktor.api)
  implementation(projects.os.server.ktor.real)
  implementation(projects.os.server.objectstore.api)
  implementation(projects.os.server.objectstore.fs)
  implementation(projects.os.server.objectstore.s3)
  implementation(projects.os.server.okhttpclient)
  implementation(projects.os.server.passkeys.real)
  implementation(projects.os.server.payments.stripe)
  implementation(projects.os.server.permits.real)
  implementation(projects.os.server.sendemail.postmark)
  implementation(projects.os.server.sql.api)
  implementation(projects.os.server.sql.real)
  implementation(projects.os.server.wasm.api)
  implementation(projects.os.server.wasm.jvm)
  implementation(projects.os.server.website.real)
  implementation(projects.os.server.wiring)
  implementation(projects.platform.api)
  implementation(projects.wasmox.wasmoxSql)
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
