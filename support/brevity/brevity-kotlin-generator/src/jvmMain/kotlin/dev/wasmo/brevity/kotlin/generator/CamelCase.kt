package dev.wasmo.brevity.kotlin.generator

/**
 * Converts a `kabob-case` to `UpperCamelCase` or `lowerCamelCase`.
 */
internal fun String.toCamelCase(upperCamel: Boolean): String {
  return buildString {
    var uppercase = upperCamel
    for (char in this@toCamelCase) {
      when (char) {
        '%' -> continue
        '-' -> {
          uppercase = true
          continue
        }

        in 'a'..'z' if uppercase -> append(char - ('a' - 'A'))
        in 'A'..'Z' if !uppercase -> append(char - ('A' - 'a'))
        else -> append(char)
      }
      uppercase = false
    }
  }
}
