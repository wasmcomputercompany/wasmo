plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
}

application {
  mainClass = "com.wasmo.ktor.development.WasmoServerDevelopment"
}

// Copy client-development.js into this project's resources.
val jsResources by configurations.creating {
  isCanBeResolved = true
  isCanBeConsumed = false
  attributes {
    attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage::class, "jsResources"))
  }
}
val copyJsResources = tasks.register<Copy>("copyJsResources") {
  from(jsResources)
  into(project.layout.buildDirectory.dir("jsResources/static/assets"))
}
sourceSets.main.configure {
  resources.srcDir(copyJsResources.map { project.layout.buildDirectory.dir("jsResources") })
}

dependencies {
  jsResources(project(":host:client:app-development"))
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
  implementation(project(":host:server:website:real"))
  implementation(project(":platform:api"))
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
