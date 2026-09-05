package com.wasmo.identifiers

@JvmInline
value class PermitType(val value: String)

val EmailAddressLinkPermitType = PermitType("EmailAddressLink")
val PasswordCheckPermitType = PermitType("PasswordCheck")
