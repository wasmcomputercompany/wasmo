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
  jsResources(project(":client:app-development"))
  implementation(libs.okio)
  implementation(libs.okhttp)
  implementation(project(":platform:api"))
  implementation(project(":server:actions"))
  implementation(project(":server:ktor"))
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
  }
}
