package com.wasmo

import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.wasmo.brevity.WitAdapter

fun <W, T> WitAdapter<W, T>.assertRoundTrip(wit: W, value: T) {
  assertThat(toWit(value)).isEqualTo(wit)
  assertThat(fromWit(wit)).isEqualTo(value)
}
