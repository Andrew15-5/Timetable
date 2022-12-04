package com.andrew.timetable

class Recess {
  val is_between_lessons: Boolean
  val is_during_lesson: Boolean
  val time_period_index: Int
  val timing_index: Int
  val lesson: Int

  private constructor(
    lesson: Int,
    time_period_index: Int,
    timing_index: Int,
    between_lessons: Boolean
  ) {
    this.lesson = lesson
    this.time_period_index = time_period_index
    this.timing_index = timing_index
    is_between_lessons = between_lessons
    is_during_lesson = !between_lessons
  }

  companion object {
    fun after(lesson: Int): Recess {
      // y = x + (x - 1) <- correlation formula
      // 1 -> 1
      // 2 -> 3
      // 3 -> 5
      // 4 -> 7
      // 5 -> 9
      val time_period_index = 2 * lesson - 1
      // y = (x - 1) * 4 + 3 <- correlation formula
      // 1 -> 3
      // 2 -> 7
      // 3 -> 11
      // 4 -> 15
      // 5 -> 19
      val timing_index = 4 * lesson - 1
      return Recess(lesson, time_period_index, timing_index, true)
    }

    fun during(lesson: Int): Recess {
      // y = x + (x - 2) <- correlation formula
      // 1 -> 0
      // 2 -> 2
      // 3 -> 4
      // 4 -> 6
      // 5 -> 8
      val time_period_index = 2 * lesson - 2
      // y = (x - 1) * 4 + 1 <- correlation formula
      // 1 -> 1
      // 2 -> 5
      // 3 -> 9
      // 4 -> 13
      // 5 -> 17
      // 6 -> 21
      val timing_index = 4 * lesson - 3
      return Recess(lesson, time_period_index, timing_index, false)
    }
  }
}
