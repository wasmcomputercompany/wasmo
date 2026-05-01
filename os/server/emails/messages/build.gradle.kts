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
        implementation(projects.os.framework)
        implementation(projects.os.server.emails.attachments)
        implementation(projects.os.server.sendemail.api)
        implementation(projects.support.okioHtml)
        implementation(projects.support.tokens)
      }
    }
    jsTest {
      dependencies {
        implementation(libs.burst.coroutines)
        implementation(libs.kotlinx.coroutines.test)
        implementation(projects.support.domTester)
      }
    }
  }
}
