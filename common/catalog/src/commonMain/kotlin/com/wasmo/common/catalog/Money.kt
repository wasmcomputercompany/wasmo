package com.wasmo.common.catalog

enum class Currency {
  CAD,
}

data class Money(
  val currency: Currency,
  val amount: Long,
) {
  init {
    require(amount >= 0L)
  }
}
