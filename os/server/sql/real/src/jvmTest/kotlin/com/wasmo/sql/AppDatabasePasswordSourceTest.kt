package com.wasmo.sql

import assertk.all
import assertk.assertThat
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import assertk.assertions.length
import assertk.assertions.prop
import assertk.assertions.size
import com.wasmo.identifiers.AppSlug
import com.wasmo.identifiers.ComputerSlug
import com.wasmo.identifiers.DatabaseSlug
import com.wasmo.identifiers.Secret
import kotlin.test.Test
import okio.ByteString.Companion.encodeUtf8

class AppDatabasePasswordSourceTest {
  private val databaseSecret = AppDatabaseSecret("rhubarb-is-the-most-well-behaved-dog".encodeUtf8())
  private val computerSlug = ComputerSlug("doghouse")
  private val appSlug = AppSlug("dogbowl")
  private val dogBowlSource = AppDatabasePasswordSource(
    secret = databaseSecret,
    computerSlug = computerSlug,
    appSlug = appSlug,
  )

  @Test
  fun `passwords have sufficient length`() {
    val password = dogBowlSource.retrievePassword(DatabaseSlug(""))
    assertThat(password).prop(Secret::rawUnredactedSecret).all {
      isNotEmpty()
      length().isGreaterThan(20)
    }
  }

  @Test
  fun `different databases have different passwords`() {
    val default = dogBowlSource.retrievePassword(DatabaseSlug(""))
    val water = dogBowlSource.retrievePassword(DatabaseSlug("water"))
    val brawndo = dogBowlSource.retrievePassword(DatabaseSlug("brawndo"))

    assertThat(listOf(default, water, brawndo))
      .extracting { it.rawUnredactedSecret }
      .transform { it.distinct() }
      .size().isEqualTo(3)
  }

  @Test
  fun `password generated twice are the same`() {
    val default = dogBowlSource.retrievePassword(DatabaseSlug(""))
    val defaultAgain = dogBowlSource.retrievePassword(DatabaseSlug(""))
    assertThat(default).prop(Secret::rawUnredactedSecret).isEqualTo(defaultAgain.rawUnredactedSecret)
  }

  @Test
  fun `different apps have different passwords`() {
    val bottleSource = AppDatabasePasswordSource(
      secret = databaseSecret,
      computerSlug = computerSlug,
      appSlug = AppSlug("bottle"),
    )
    val bowlDefault = dogBowlSource.retrievePassword(DatabaseSlug(""))
    val bowlWater = dogBowlSource.retrievePassword(DatabaseSlug("water"))
    val bottleDefault = bottleSource.retrievePassword(DatabaseSlug(""))
    val bottleWater = bottleSource.retrievePassword(DatabaseSlug("water"))

    assertThat(listOf(bowlDefault, bowlWater, bottleDefault, bottleWater))
      .extracting { it.rawUnredactedSecret }
      .transform { it.distinct() }
      .size().isEqualTo(4)
  }

  @Test
  fun `different computers have different passwords`() {
    val bottleSource = AppDatabasePasswordSource(
      secret = databaseSecret,
      computerSlug = ComputerSlug("dogpark"),
      appSlug = appSlug,
    )
    val houseDefault = dogBowlSource.retrievePassword(DatabaseSlug(""))
    val houseWater = dogBowlSource.retrievePassword(DatabaseSlug("water"))
    val parkDefault = bottleSource.retrievePassword(DatabaseSlug(""))
    val parkWater = bottleSource.retrievePassword(DatabaseSlug("water"))

    assertThat(listOf(houseDefault, houseWater, parkDefault, parkWater))
      .extracting { it.rawUnredactedSecret }
      .transform { it.distinct() }
      .size().isEqualTo(4)
  }
}
