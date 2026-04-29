package wasmo.sql

actual annotation class Language(
  actual val value: String,
  actual val prefix: String = "",
  actual val suffix: String = "",
)
