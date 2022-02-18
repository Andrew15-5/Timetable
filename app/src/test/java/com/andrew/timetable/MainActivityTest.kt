package com.andrew.timetable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Month

class MainActivityTest {
  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  lateinit var main_activity: MainActivity

  @Before
  fun setUp() {
    main_activity = MainActivity()
  }

  @Test
  fun test_get_week_of_year() {
    val year = 2021
    for (day in 1..2) {
      val month = Month.JANUARY.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 1) // 18+ week
    }
    for (day in 8..14) {
      val month = Month.FEBRUARY.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 6 + 1) // 1st week
    }
    for (day in 31..31) {
      val month = Month.MAY.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 6 + 17) // 17th week
    }
    for (day in 1..6) {
      val month = Month.JUNE.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 6 + 17) // 17th week
    }
    for (day in 7..13) {
      val month = Month.JUNE.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 6 + 18) // 18+ week
    }
    for (day in 1..5) {
      val month = Month.SEPTEMBER.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 35 + 1) // 1st week
    }
    for (day in 6..12) {
      val month = Month.SEPTEMBER.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 35 + 2) // 2nd week
    }
    for (day in 20..26) {
      val month = Month.DECEMBER.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 35 + 17) // 17th week
    }
    for (day in 27..31) {
      val month = Month.DECEMBER.value
      assertEquals(main_activity.get_week_of_year(year, month, day), 53) // 18+ week
    }
  }
}
