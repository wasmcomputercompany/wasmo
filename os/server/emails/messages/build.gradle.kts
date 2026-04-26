plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.burst)
  id("wasmo-build")
  id("dom-tester")
}

wasmoBuild {
  libraryJvmJs()
}

domTester {
  domTester()
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.okio)
        implementation(libs.kotlinx.html)
        implementation(project(":os:framework"))
        implementation(project(":os:server:emails:attachments"))
        implementation(project(":os:server:sendemail:api"))
        implementation(project(":support:okio-html"))
        implementation(project(":support:tokens"))
      }
    }
    jsTest {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(project(":support:dom-tester"))
      }
    }
  }
}
