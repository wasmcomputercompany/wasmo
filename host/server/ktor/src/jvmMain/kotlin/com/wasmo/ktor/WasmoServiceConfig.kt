package com.wasmo.ktor

import com.wasmo.accounts.SessionCookieSpec
import com.wasmo.common.catalog.Catalog
import com.wasmo.deployment.Deployment
import com.wasmo.objectstore.ObjectStoreAddress
import com.wasmo.sendemail.postmark.PostmarkCredentials
import com.wasmo.stripe.StripeCredentials
import okio.ByteString

data class WasmoServiceConfig(
  val cookieSecret: ByteString,
  val postmarkCredentials: PostmarkCredentials,
  val stripeCredentials: StripeCredentials,
  val catalog: Catalog,
  val postgresDatabaseHostname: String,
  val postgresDatabaseName: String,
  val postgresDatabaseUser: String,
  val postgresDatabasePassword: String,
  val deployment: Deployment,
  val objectStoreAddress: ObjectStoreAddress,
  val sessionCookieSpec: SessionCookieSpec,
)
