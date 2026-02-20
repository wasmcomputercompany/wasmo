plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
}

application {
  mainClass = "com.wasmo.ktor.staging.WasmoServerStaging"
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
  jsResources(project(":client:app-staging"))
  implementation(libs.okhttp)
  implementation(libs.okio)
  implementation(project(":common:catalog"))
  implementation(project(":platform:api"))
  implementation(project(":server:accounts"))
  implementation(project(":server:computers"))
  implementation(project(":server:deployment"))
  implementation(project(":server:ktor"))
  implementation(project(":server:sendemail"))
  implementation(project(":server:stripe"))
  implementation(project(":server:website"))
}

ktor {
  docker {
    jreVersion.set(JavaVersion.VERSION_24)
    localImageName.set("wasmo-staging")
    imageTag.set("latest")
  }
}
