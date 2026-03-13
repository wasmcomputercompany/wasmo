package app.cash.sqldelight.driver.r2dbc

/**
 * SQLDelight's generated code assumes the driver implementation is its own.
 *
 * But we're running SQLDelight on our own driver that uses Wasmo's SQL interface. So trick it into
 * working by implementing the expected marker interface.
 */
interface R2dbcCursor
interface R2dbcPreparedStatement
