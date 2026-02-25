package com.wasmo.client.app.routing

enum class TransitionDirection {
  /** Push the current screen onto the back stack and navigate left-to-right. */
  PUSH,

  /** Pop the current screen from the back stack, and navigate left-to-right. */
  REPLACE,

  /** Pop the current screen off the back stack and navigate right-to-left. */
  POP
}
