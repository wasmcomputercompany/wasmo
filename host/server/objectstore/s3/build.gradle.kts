plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.noarg)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.metro)
  id("wasmo-build")
}

wasmoBuild {
  libraryJvm()
}

noArg {
  annotation("jakarta.xml.bind.annotation.XmlAccessorType")
}

kotlin {
  sourceSets {
    val jvmMain by getting {
      dependencies {
        implementation(libs.jaxb.api)
        implementation(libs.jaxb.implementation)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.okio)
        implementation(libs.okhttp)
        implementation(libs.okhttp.coroutines)
        implementation(libs.retrofit)
        implementation(libs.retrofit.converter.kotlinx.serialization)
        implementation(libs.retrofit.converter.jaxb3)
        implementation(project(":host:server:objectstore:api"))
        implementation(project(":platform:api"))
      }
    }
    val jvmTest by getting {
      dependencies {
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":platform:testing"))
      }
    }
  }
}
