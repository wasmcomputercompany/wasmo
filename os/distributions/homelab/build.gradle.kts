plugins {
  id("org.gradle.application")
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.metro)
  alias(libs.plugins.jib)
  id("wasmo-build")
}

wasmoBuild {
  consumeJsResources("static/assets")
}

application {
  mainClass = "com.wasmo.distributions.homelab.HomelabMain"
  applicationDefaultJvmArgs = listOf(
    "-server",
    "-Djava.awt.headless=true",
    "-Xms512m",
    "-Xmx512m",
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:54402",
  )
}

jib {
  from {
    // We've got a choice:
    //  * local-platform-only binaries for development (faster)
    //  * multi-platform binaries for publishing (slow)
    if (rootProject.findProperty("docker.platforms") == "local") {
      image = "docker://wasmo/homelab-foundation"
    } else {
      image = "wasmo/homelab-foundation"
      platforms {
        platform {
          architecture = "amd64"
          os = "linux"
        }
        platform {
          architecture = "arm64"
          os = "linux"
        }
      }
    }
  }
  to {
    image = "wasmo/homelab"
    tags = setOf("latest")
  }
  container {
    mainClass = application.mainClass.get()
    jvmFlags = application.applicationDefaultJvmArgs.toList()

    // TODO(jwilson): these secrets aren't used. Stop requiring them.
    environment = mapOf(
      "POSTMARK_SERVER_TOKEN" to ("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"),
    )
  }
}

dependencies {
  add("jsResources", projects.os.client.appHomelab)
  implementation(libs.clikt)
  implementation(libs.clikt.core)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.debug)
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
  implementation(projects.os.logging)
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
  implementation(projects.os.server.passwords.api)
  implementation(projects.os.server.passwords.real)
  implementation(projects.os.server.permits.real)
  implementation(projects.os.server.postgresqloperator.api)
  implementation(projects.os.server.postgresqloperator.exec)
  implementation(projects.os.server.postgresqloperator.external)
  implementation(projects.os.server.sendemail.postmark)
  implementation(projects.os.server.sql.api)
  implementation(projects.os.server.sql.real)
  implementation(projects.os.server.usernames.real)
  implementation(projects.os.server.wasm.api)
  implementation(projects.os.server.wasm.jvm)
  implementation(projects.os.server.website.real)
  implementation(projects.os.server.wiring)
  implementation(projects.platform.api)
  implementation(projects.wasmox.wasmoxSql)
}
