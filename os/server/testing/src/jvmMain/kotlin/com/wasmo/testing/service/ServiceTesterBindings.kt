package com.wasmo.testing.service

import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.computers.AppCatalog
import com.wasmo.events.EventListener
import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.MDN
import com.wasmo.identifiers.Deployment
import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.HostnamePatterns
import com.wasmo.identifiers.OsScope
import com.wasmo.identifiers.hostnamePatterns
import com.wasmo.payments.PaymentsService
import com.wasmo.sendemail.SendEmailService
import com.wasmo.testing.FakeAppPublisher
import com.wasmo.testing.FakePaymentsService
import com.wasmo.testing.FakeSendEmailService
import com.wasmo.testing.apps.SampleApps
import com.wasmo.testing.events.TestEventListener
import com.wasmo.wasm.AppLoader
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlin.time.Clock
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okio.ByteString.Companion.encodeUtf8
import wasmo.http.FakeHttpService
import wasmo.http.HttpService
import wasmo.objectstore.FakeObjectStore
import wasmo.objectstore.ObjectStore
import wasmo.time.FakeClock

@BindingContainer
abstract class ServiceTesterBindings {

  @Binds
  abstract fun bindClock(real: FakeClock): Clock

  @Binds
  abstract fun bindSendEmailService(real: FakeSendEmailService): SendEmailService

  @Binds
  abstract fun bindPaymentsService(real: FakePaymentsService): PaymentsService

  @Binds
  abstract fun bindHttpClient(real: FakeHttpService): HttpService

  @Binds
  abstract fun bindEventListener(real: TestEventListener): EventListener

  @Binds
  abstract fun bindAppLoader(real: FakeAppPublisher): AppLoader

  @Binds
  @ForOs
  abstract fun bindObjectStore(real: FakeObjectStore): ObjectStore

  companion object {

    @Provides
    @SingleIn(OsScope::class)
    fun provideDeployment(): Deployment = Deployment(
      baseUrl = "https://wasmo.com/".toHttpUrl(),
      sendFromEmailAddress = "noreply@wasmo.com",
    )

    @Provides
    @SingleIn(OsScope::class)
    fun provideClock(): FakeClock = FakeClock()

    @Provides
    @SingleIn(OsScope::class)
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @SingleIn(OsScope::class)
    fun provideFakeHttpClient(
      appPublisher: FakeAppPublisher,
    ): FakeHttpService = FakeHttpService().apply {
      this += appPublisher.httpHandler
    }

    @Provides
    @SingleIn(OsScope::class)
    fun provideStripePublishableKey(): StripePublishableKey =
      StripePublishableKey("pk_test_5544332211")

    @Provides
    @SingleIn(OsScope::class)
    fun provideSessionCookieSpec(): SessionCookieSpec = SessionCookieSpec.Https

    @Provides
    @SingleIn(OsScope::class)
    fun provideCookieSecret(): CookieSecret = CookieSecret("secret".encodeUtf8())

    @Provides
    @SingleIn(OsScope::class)
    fun provideAppCatalog(
      sampleApps: SampleApps,
    ): AppCatalog = sampleApps.appCatalog

    @Provides
    @SingleIn(OsScope::class)
    fun provideFakeObjectStore(): FakeObjectStore = FakeObjectStore()

    @Provides
    @SingleIn(OsScope::class)
    fun provideContentTypeDatabase(): ContentTypeDatabase = ContentTypeDatabase.MDN

    @Provides
    @SingleIn(OsScope::class)
    fun provideHostnamePatterns(deployment: Deployment): HostnamePatterns =
      deployment.hostnamePatterns()
  }
}
