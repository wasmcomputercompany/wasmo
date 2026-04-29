package wasmo.access

data class Caller(
  /**
   * Identify the authenticated user to this application. This is scoped to the application.
   * This is null if the caller's access is [ComputerAccess.Anonymous].
   */
  val userId: Long?,

  /** The access the caller has over the computer that this application is installed on. */
  val computerAccess: ComputerAccess,

  val userAgent: String?,
  val ip: String?,
)
