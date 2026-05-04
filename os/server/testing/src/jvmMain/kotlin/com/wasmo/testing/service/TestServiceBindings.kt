package com.wasmo.testing.service

import com.wasmo.accounts.ClientAuthenticator
import com.wasmo.accounts.CookieSecret
import com.wasmo.accounts.RealClientAuthenticator
import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.api.stripe.StripePublishableKey
import com.wasmo.computers.AppCatalog
import com.wasmo.deployment.Deployment
import com.wasmo.events.EventListener
import com.wasmo.framework.ContentTypeDatabase
import com.wasmo.framework.MDN
import com.wasmo.identifiers.ForOs
import com.wasmo.identifiers.OsScope
import com.wasmo.payments.PaymentsService
import com.wasmo.permits.PermitService
import com.wasmo.permits.RealPermitService
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
interface TestServiceBindings {

  @Binds
  fun bindClock(real: FakeClock): Clock

  @Binds
  fun bindSendEmailService(real: FakeSendEmailService): SendEmailService

  @Binds
  fun bindPaymentsService(real: FakePaymentsService): PaymentsService

  @Binds
  fun bindHttpClient(real: FakeHttpService): HttpService

  @Binds
  fun bindClientAuthenticatorFactory(
    real: RealClientAuthenticator.Factory,
  ): ClientAuthenticator.Factory

  @Binds
  fun bindEventListener(real: TestEventListener): EventListener

  @Binds
  fun bindAppLoader(real: FakeAppPublisher): AppLoader

  @Binds
  @ForOs
  fun bindObjectStore(real: FakeObjectStore): ObjectStore

  @Binds
  fun bindPermitService(real: RealPermitService): PermitService

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
  }
}
