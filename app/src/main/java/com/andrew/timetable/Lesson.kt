package com.andrew.timetable

import java.security.InvalidParameterException

/**
 * @param ordinal lesson's ordinal number
 * @param half [[1;2]] (first or second half of lesson)
 * @throws InvalidParameterException if half value is invalid
 */
class Lesson(ordinal: Int, half: Int) {
  val half: Int
  val lesson: Int
  val time_period_index: Int
  val timing_index: Int

  init {
    this.half = half
    lesson = ordinal
    // y = x + (x - 2) <- correlation formula
    // 1 -> 0
    // 2 -> 2
    // 3 -> 4
    // 4 -> 6
    // 5 -> 8
    time_period_index = 2 * lesson - 2
    timing_index = when (half) {
      // y = (x - 1) * 4 <- correlation formula
      // 1 -> 0
      // 2 -> 4
      // 3 -> 8
      // 4 -> 12
      // 5 -> 16
      // 6 -> 20
      1 -> 4 * lesson - 4
      // y = (x - 1) * 4 + 2 <- correlation formula
      // 1 -> 2
      // 2 -> 6
      // 3 -> 10
      // 4 -> 14
      // 5 -> 18
      // 6 -> 22
      2 -> 4 * lesson - 2
      else -> throw InvalidParameterException(
        "half parameter can be either 1 or 2"
      )
    }
  }
}
