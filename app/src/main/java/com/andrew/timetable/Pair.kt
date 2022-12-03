package com.andrew.timetable

import java.security.InvalidParameterException

/**
 * @param ordinal pair's ordinal order
 * @param lesson [[1;2]] (first or second part of pair)
 * @throws InvalidParameterException if lesson value is invalid
 */
class Pair(ordinal: Int, lesson: Int) {
  val pair: Int
  val time_period_index: Int
  val timing_index: Int

  init {
    pair = ordinal
    // y = x + (x - 2) <- correlation formula
    // 1 -> 0
    // 2 -> 2
    // 3 -> 4
    // 4 -> 6
    // 5 -> 8
    time_period_index = 2 * pair - 2
    timing_index = when (lesson) {
      // y = (x - 1) * 4 <- correlation formula
      // 1 -> 0
      // 2 -> 4
      // 3 -> 8
      // 4 -> 12
      // 5 -> 16
      // 6 -> 20
      1 -> 4 * pair - 4
      // y = (x - 1) * 4 + 2 <- correlation formula
      // 1 -> 2
      // 2 -> 6
      // 3 -> 10
      // 4 -> 14
      // 5 -> 18
      // 6 -> 22
      2 -> 4 * pair - 2
      else -> throw InvalidParameterException(
        "lesson parameter can be either 1 or 2"
      )
    }
  }
}