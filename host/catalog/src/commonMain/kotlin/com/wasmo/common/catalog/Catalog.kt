package com.wasmo.common.catalog

data class Product(
  val productId: String,
  val priceId: String,
  val money: Money,
) {
  init {
    require(productId.startsWith("prod_"))
    require(priceId.startsWith("price_"))
  }
}

class Catalog(
  val wasmoStandard: Product,
)
