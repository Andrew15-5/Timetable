package com.andrew.timetable

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month

class UtilsTest {
  val utils = Utils()

  class NoTime {
    companion object {
      const val get_half_time_left = "     --:--  "
      const val get_lessons_time_left = "      --:--  "
      const val get_time_until_next_half = "     --:--"
      const val get_time_until_next_lesson = "    --:--"
    }
  }

  fun get_time(timing_index: Int): Time {
    return utils.timings[timing_index]
  }

  fun get_time(timing_index: Int, hhmm_offset: String): Time {
    return utils.timings[timing_index] + Time.from_hhmm(hhmm_offset)
  }

  fun expected_time(hhmm: String): String {
    return Time.from_hhmm(hhmm).short_format()
  }

  // --------------------------|get_half_time_left()|---------------------------
  @Test
  fun test_get_half_time_left_during_other_times() {
    arrayOf(
      "0:00",
      "4:00",
      "8:29",
      "19:25",
      "22:22",
      "23:59"
    ).forEach { hhmm ->
      assertEquals(
        NoTime.get_half_time_left,
        utils.get_half_time_left(Time.from_hhmm(hhmm))
      )
    }
  }

  @Test
  fun test_get_half_time_left_during_recess() {
    for (i in 1 until utils.timings.size - 1 step 2) {
      arrayOf(
        "0:0",
        "0:01",
        "0:02",
        "0:03",
        "0:04"
      ).forEach { offset ->
        assertEquals(
          NoTime.get_half_time_left,
          utils.get_half_time_left(get_time(i, offset))
        )
      }
    }
  }

  @Test
  fun test_get_half_time_left_during_study_time() {
    for (i in 0 until utils.timings.size - 1 step 2) {
      mapOf(
        "0:45" to "0:00",
        "0:44" to "0:01",
        "0:23" to "0:22",
        "0:01" to "0:44",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_half_time_left(get_time(i, y))
        )
      }
    }
  }
  // --------------------------|get_half_time_left()|---------------------------

  // -----------------------|get_time_until_next_half()|------------------------
  @Test
  fun test_get_time_until_next_half_before_study_time() {
    mapOf(
      "8:30" to "0:00",
      "4:30" to "4:00",
      "0:01" to "8:29",
    ).forEach { (x, y) ->
      assertEquals(
        expected_time(x),
        utils.get_time_until_next_half(Time.from_hhmm(y))
      )
    }
  }

  @Test
  fun test_get_time_until_next_half_after_study_time() {
    val last_index = utils.timings.size - 1
    for (i in 1 downTo 0) {
      assertEquals(
        NoTime.get_time_until_next_half,
        utils.get_time_until_next_half(get_time(last_index - i))
      )
    }
    arrayOf(
      "19:25",
      "22:22",
      "23:59"
    ).forEach { hhmm ->
      assertEquals(
        NoTime.get_time_until_next_half,
        utils.get_time_until_next_half(Time.from_hhmm(hhmm))
      )
    }
  }

  @Test
  fun test_get_time_until_next_half_during_1st_half_of_lesson() {
    for (i in 0 until utils.timings.size - 3 step 4) {
      mapOf(
        "0:50" to "0:00",
        "0:49" to "0:01",
        "0:28" to "0:22",
        "0:06" to "0:44",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_half(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_half_during_2nd_half_of_lesson() {
    for (i in 2 until utils.timings.size - 5 step 4) {
      val lesson = utils.get_lesson(get_time(i))
      when (lesson!!.lesson) {
        3 -> mapOf(
          "1:15" to "0:00", // 45 + 30
          "1:14" to "0:01",
          "0:53" to "0:22",
          "0:31" to "0:44",
        )
        5 -> mapOf(
          "0:55" to "0:00", // 45 + 10
          "0:54" to "0:01",
          "0:33" to "0:22",
          "0:11" to "0:44",
        )
        else -> mapOf(
          "1:00" to "0:00", // 45 + 15
          "0:59" to "0:01",
          "0:38" to "0:22",
          "0:16" to "0:44",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_half(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_half_during_recess_that_is_during_a_lesson() {
    for (i in 1 until utils.timings.size - 2 step 4) {
      mapOf(
        "0:05" to "0:00",
        "0:04" to "0:01",
        "0:03" to "0:02",
        "0:01" to "0:04",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_half(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_half_during_recess_between_lessons() {
    for (i in 3 until utils.timings.size - 4 step 4) {
      val recess = utils.get_recess(get_time(i))
      when (recess!!.lesson) {
        3 -> mapOf(
          "0:30" to "0:00",
          "0:29" to "0:01",
          "0:15" to "0:15",
          "0:01" to "0:29",
        )
        5 -> mapOf(
          "0:10" to "0:00",
          "0:09" to "0:01",
          "0:05" to "0:05",
          "0:01" to "0:09",
        )
        else -> mapOf(
          "0:15" to "0:00",
          "0:14" to "0:01",
          "0:08" to "0:07",
          "0:01" to "0:14",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_half(get_time(i, y))
        )
      }
    }
  }
  // -----------------------|get_time_until_next_half()|------------------------

  // -------------------------|get_lesson_time_left()|--------------------------
  @Test
  fun test_get_lessons_time_left_no_time_during_other_times() {
    arrayOf(
      "0:00",
      "4:00",
      "8:29",
      "19:25",
      "22:22",
      "23:59"
    ).forEach { hhmm ->
      assertEquals(
        NoTime.get_lessons_time_left,
        utils.get_lessons_time_left(Time.from_hhmm(hhmm))
      )
    }
  }

  @Test
  fun test_get_lessons_time_left_no_time_during_recess_between_lessons() {
    for (i in 3 until utils.timings.size step 4) {
      arrayOf(
        "0:0",
        "0:01",
        "0:02",
        "0:03",
        "0:04"
      ).forEach { offset ->
        assertEquals(
          NoTime.get_lessons_time_left,
          utils.get_lessons_time_left(get_time(i, offset))
        )
      }
    }
  }

  @Test
  fun test_get_lessons_time_left_during_recess_that_is_during_a_lesson() {
    for (i in 1 until utils.timings.size - 2 step 4) {
      val recess = utils.get_recess(get_time(i))
      if (recess!!.lesson >= 6) break
      mapOf(
        "0:50" to "0:00", // 5 + 45
        "0:49" to "0:01",
        "0:48" to "0:02",
        "0:46" to "0:04",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_lessons_time_left(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_lessons_time_left_during_1st_half_of_lesson() {
    for (i in 0 until utils.timings.size - 3 step 4) {
      mapOf(
        "1:35" to "0:00", // 45 + 5 + 45
        "1:34" to "0:01",
        "1:13" to "0:22",
        "0:51" to "0:44",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_lessons_time_left(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_lessons_time_left_during_2nd_half_of_lesson() {
    for (i in 2 until utils.timings.size - 1 step 4) {
      mapOf(
        "0:45" to "0:00",
        "0:44" to "0:01",
        "0:23" to "0:22",
        "0:01" to "0:44",
      ).forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_lessons_time_left(get_time(i, y))
        )
      }
    }
  }
  // -------------------------|get_lesson_time_left()|--------------------------

  // ----------------------|get_time_until_next_lesson()|-----------------------
  @Test
  fun test_get_time_until_next_lesson_before_study_time() {
    mapOf(
      "8:30" to "0:00",
      "4:30" to "4:00",
      "0:01" to "8:29",
    ).forEach { (x, y) ->
      assertEquals(
        expected_time(x),
        utils.get_time_until_next_lesson(Time.from_hhmm(y))
      )
    }
  }

  @Test
  fun test_get_time_until_next_lesson_after_study_time() {
    val last_index = utils.timings.size - 1
    for (i in 3 downTo 0) {
      assertEquals(
        NoTime.get_time_until_next_lesson,
        utils.get_time_until_next_lesson(get_time(last_index - i))
      )
    }
    arrayOf(
      "19:25",
      "22:22",
      "23:59"
    ).forEach { hhmm ->
      assertEquals(
        NoTime.get_time_until_next_lesson,
        utils.get_time_until_next_lesson(Time.from_hhmm(hhmm))
      )
    }
  }

  @Test
  fun test_get_time_until_next_lesson_during_1st_half_of_lesson() {
    for (i in 0 until utils.timings.size - 7 step 4) {
      val lesson = utils.get_lesson(get_time(i))
      when (lesson!!.lesson) {
        3 -> mapOf(
          "2:05" to "0:00", // 45 + 5 + 45 + 30
          "2:04" to "0:01",
          "1:43" to "0:22",
          "1:21" to "0:44",
        )
        5 -> mapOf(
          "1:45" to "0:00", // 45 + 5 + 45 + 10
          "1:44" to "0:01",
          "1:23" to "0:22",
          "1:01" to "0:44",
        )
        else -> mapOf(
          "1:50" to "0:00", // 45 + 5 + 45 + 15
          "1:49" to "0:01",
          "1:28" to "0:22",
          "1:06" to "0:44",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_lesson(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_lesson_during_2nd_half_of_lesson() {
    for (i in 2 until utils.timings.size - 5 step 4) {
      val lesson = utils.get_lesson(get_time(i))
      when (lesson!!.lesson) {
        3 -> mapOf(
          "1:15" to "0:00", // 45 + 30
          "1:14" to "0:01",
          "0:53" to "0:22",
          "0:31" to "0:44",
        )
        5 -> mapOf(
          "0:55" to "0:00", // 45 + 10
          "0:54" to "0:01",
          "0:33" to "0:22",
          "0:11" to "0:44",
        )
        else -> mapOf(
          "1:00" to "0:00", // 45 + 15
          "0:59" to "0:01",
          "0:38" to "0:22",
          "0:16" to "0:44",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_lesson(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_lesson_during_recess_between_lessons() {
    for (i in 3 until utils.timings.size - 4 step 4) {
      val recess = utils.get_recess(get_time(i))
      when (recess!!.lesson) {
        3 -> mapOf(
          "0:30" to "0:00",
          "0:29" to "0:01",
          "0:15" to "0:15",
          "0:01" to "0:29",
        )
        5 -> mapOf(
          "0:10" to "0:00",
          "0:09" to "0:01",
          "0:05" to "0:05",
          "0:01" to "0:09",
        )
        else -> mapOf(
          "0:15" to "0:00",
          "0:14" to "0:01",
          "0:08" to "0:07",
          "0:01" to "0:14",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_lesson(get_time(i, y))
        )
      }
    }
  }

  @Test
  fun test_get_time_until_next_lesson_during_recess_that_is_during_a_lesson() {
    for (i in 1 until utils.timings.size - 6 step 4) {
      val recess = utils.get_recess(get_time(i))
      when (recess!!.lesson) {
        3 -> mapOf(
          "1:20" to "0:00", // 5 + 45 + 30
          "1:19" to "0:01",
          "1:18" to "0:02",
          "1:16" to "0:04",
        )
        5 -> mapOf(
          "1:00" to "0:00", // 5 + 45 + 10
          "0:59" to "0:01",
          "0:58" to "0:02",
          "0:56" to "0:04",
        )
        else -> mapOf(
          "1:05" to "0:00", // 5 + 45 + 15
          "1:04" to "0:01",
          "1:03" to "0:02",
          "1:01" to "0:04",
        )
      }.forEach { (x, y) ->
        assertEquals(
          expected_time(x),
          utils.get_time_until_next_lesson(get_time(i, y))
        )
      }
    }
  }
  // ----------------------|get_time_until_next_lesson()|-----------------------

  @Test
  fun test_get_week_of_year() {
    fun f(year: Int, month: Int, day: Int): Int {
      return utils.get_week_of_year(year, month, day)
    }

    val year = 2021
    for (day in 1..2) {
      val month = Month.JANUARY.value
      assertEquals(1, f(year, month, day)) // 18+ week
    }
    for (day in 8..14) {
      val month = Month.FEBRUARY.value
      assertEquals(6 + 1, f(year, month, day)) // 1st week
    }
    for (day in 31..31) {
      val month = Month.MAY.value
      assertEquals(6 + 17, f(year, month, day)) // 17th week
    }
    for (day in 1..6) {
      val month = Month.JUNE.value
      assertEquals(6 + 17, f(year, month, day)) // 17th week
    }
    for (day in 7..13) {
      val month = Month.JUNE.value
      assertEquals(6 + 18, f(year, month, day)) // 18+ week
    }
    for (day in 1..5) {
      val month = Month.SEPTEMBER.value
      assertEquals(35 + 1, f(year, month, day)) // 1st week
    }
    for (day in 6..12) {
      val month = Month.SEPTEMBER.value
      assertEquals(35 + 2, f(year, month, day)) // 2nd week
    }
    for (day in 20..26) {
      val month = Month.DECEMBER.value
      assertEquals(35 + 17, f(year, month, day)) // 17th week
    }
    for (day in 27..31) {
      val month = Month.DECEMBER.value
      assertEquals(53, f(year, month, day)) // 18+ week
    }
  }
}
