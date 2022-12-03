package com.andrew.timetable

class Break {
  val is_between_pairs: Boolean
  val is_during_pair: Boolean
  val time_period_index: Int
  val timing_index: Int
  val pair: Int

  private constructor(
    pair: Int,
    time_period_index: Int,
    timing_index: Int,
    between_pairs: Boolean
  ) {
    this.pair = pair
    this.time_period_index = time_period_index
    this.timing_index = timing_index
    is_between_pairs = between_pairs
    is_during_pair = !between_pairs
  }

  companion object {
    fun after(pair: Int): Break {
      // y = x + (x - 1) <- correlation formula
      // 1 -> 1
      // 2 -> 3
      // 3 -> 5
      // 4 -> 7
      // 5 -> 9
      val time_period_index = 2 * pair - 1
      // y = (x - 1) * 4 + 3 <- correlation formula
      // 1 -> 3
      // 2 -> 7
      // 3 -> 11
      // 4 -> 15
      // 5 -> 19
      val timing_index = 4 * pair - 1
      return Break(pair, time_period_index, timing_index, true)
    }

    fun during(pair: Int): Break {
      // y = x + (x - 2) <- correlation formula
      // 1 -> 0
      // 2 -> 2
      // 3 -> 4
      // 4 -> 6
      // 5 -> 8
      val time_period_index = 2 * pair - 2
      // y = (x - 1) * 4 + 1 <- correlation formula
      // 1 -> 1
      // 2 -> 5
      // 3 -> 9
      // 4 -> 13
      // 5 -> 17
      // 6 -> 21
      val timing_index = 4 * pair - 3
      return Break(pair, time_period_index, timing_index, false)
    }
  }
}