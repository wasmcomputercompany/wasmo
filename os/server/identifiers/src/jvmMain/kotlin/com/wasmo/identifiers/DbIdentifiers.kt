package com.wasmo.identifiers

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class AccountId(val id: Long)

@Serializable
@JvmInline
value class InstalledAppId(val id: Long)

@Serializable
@JvmInline
value class InstalledAppReleaseId(val id: Long)

@Serializable
@JvmInline
value class CookieId(val id: Long)

@Serializable
@JvmInline
value class ComputerAccessId(val id: Long)

@Serializable
@JvmInline
value class ComputerAllocationId(val id: Long)

@Serializable
@JvmInline
value class ComputerId(val id: Long)

@Serializable
@JvmInline
value class ComputerSpecId(val id: Long)

@Serializable
@JvmInline
value class StripeCustomerId(val id: Long)

@Serializable
@JvmInline
value class InviteId(val id: Long)

@Serializable
@JvmInline
value class PasskeyId(val id: Long)
