package com.wasmo.common.catalog

/**
 * This catalog uses data from the 'Wasmo Sandbox' in Stripe.
 */
val DevelopmentCatalog = Catalog(
  wasmoStandard = Product(
    productId = "prod_U11HEFBqPEssaD",
    priceId = "price_1T2zFDLWsDCkq70we8Ge8204",
    money = Money(Currency.CAD, 500),
  ),
)
