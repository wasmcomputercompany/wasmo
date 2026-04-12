package com.wasmo.support.absurd

import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

@Serializable
data class MenuItem(
  val name: String,
)

@Serializable
data class Sandwich(
  val bread: String,
  val toppings: List<String>,
  val toasted: Boolean,
)

class SandwichMaker(
  private val log: Log,
) : TaskHandler<MenuItem, Sandwich> {
  val availableToppings = mutableListOf(
    "bacon",
    "jam",
    "lettuce",
    "peanut butter",
    "rye",
    "tomato",
    "white",
  )

  context(context: TaskHandler.Context)
  override suspend fun handle(params: MenuItem): Sandwich {
    val bread = context.step("select-bread") {
      val selected = when {
        "on rye" in params.name -> "rye"
        else -> "white"
      }
      log.log("taking bread: $selected")
      check(selected in availableToppings) { "no such bread: $selected" }
      return@step selected
    }

    val toppings = context.step("select-toppings") {
      val selected = when {
        "BLT" in params.name -> listOf("bacon", "lettuce", "tomato")
        "PBJ" in params.name -> listOf("peanut butter", "jam")
        else -> listOf("ham")
      }
      log.log("taking toppings: $selected")
      for (topping in selected) {
        check(topping in availableToppings) { "no such topping: $topping" }
      }
      return@step selected
    }

    val toasted = when {
      "toasted" in params.name -> {
        log.log("toasting for 30 seconds")
        context.sleepFor("toast", 30.seconds)
        true
      }
      else -> false
    }

    return Sandwich(
      bread = bread,
      toppings = toppings,
      toasted = toasted,
    )
  }

  companion object {
    val TaskName = TaskName<MenuItem, Sandwich>("SandwichMaker")
  }
}
