package com.wasmo.packaging

import dev.eav.tomlkt.Toml

val WasmoToml = Toml {
  ignoreUnknownKeys = true
  explicitNulls = false
}
