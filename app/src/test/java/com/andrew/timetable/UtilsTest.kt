package com.andrew.timetable

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Month

class UtilsTest {
  val utils = Utils()

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
