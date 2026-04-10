package com.wasmo.support.absurd

import kotlinx.coroutines.channels.Channel
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

class SandwichMaker : TaskHandler<MenuItem, Sandwich> {
  val availableToppings = mutableListOf(
    "bacon",
    "jam",
    "lettuce",
    "peanut butter",
    "rye",
    "tomato",
    "white",
  )

  val log = Channel<String>(capacity = Int.MAX_VALUE)

  context(context: TaskHandler.Context<MenuItem, Sandwich>)
  override suspend fun handle(params: MenuItem): Sandwich {
    val bread = context.step("select-bread") {
      val selected = when {
        "on rye" in params.name -> "rye"
        else -> "white"
      }
      log.send("taking bread: $selected")
      check(selected in availableToppings) { "no such bread: $selected" }
      return@step selected
    }

    val toppings = context.step("select-toppings") {
      val selected = when {
        "BLT" in params.name -> listOf("bacon", "lettuce", "tomato")
        "PBJ" in params.name -> listOf("peanut butter", "jam")
        else -> listOf("ham")
      }
      log.send("taking toppings: $selected")
      for (topping in selected) {
        check(topping in availableToppings) { "no such topping: $topping" }
      }
      return@step selected
    }

    val toasted = context.step("toast") {
      if ("toasted" !in params.name) return@step false
      log.send("toasting")
      // TODO: sleep
      true
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
