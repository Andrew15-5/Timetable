package com.andrew.timetable

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.*

class Utils {
  // Each line has 4 timings and represent nth pair ([1;6])
  private val timings_str = arrayOf(
    "8:30", "9:15", "9:20", "10:05",
    "10:20", "11:05", "11:10", "11:55",
    "12:10", "12:55", "13:00", "13:45",
    "14:15", "15:00", "15:05", "15:50",
    "16:05", "15:50", "16:55", "17:40",
    "17:50", "18:35", "18:40", "19:25"
  )
  val timings = timings_str.map { time -> Time.from_hhmm(time) }
  val time_periods: List<String>

  init {
    val mutable_time_periods = mutableListOf<String>()
    val group_by = 4 // Each pair has 4 timings
    for (i in timings_str.indices step group_by) {
      val start = timings_str[i]
      val break_start = timings_str[i + 1]
      val break_end = timings_str[i + 2]
      val end = timings_str[i + 3]
      mutable_time_periods += "$start-$break_start | $break_end-$end"
      if (i + 4 >= timings_str.size) continue
      val next_start = timings_str[i + 4]
      mutable_time_periods += "$end-$next_start"
    }
    time_periods = mutable_time_periods.toList()
  }

  fun get_break(time: Time): Int? {
    return when {
      time.from_until(timings[1], timings[2]) -> 1
      time.from_until(timings[3], timings[4]) -> 12
      time.from_until(timings[5], timings[6]) -> 2
      time.from_until(timings[7], timings[8]) -> 23
      time.from_until(timings[9], timings[10]) -> 3
      time.from_until(timings[11], timings[12]) -> 34
      time.from_until(timings[13], timings[14]) -> 4
      time.from_until(timings[15], timings[16]) -> 45
      time.from_until(timings[17], timings[18]) -> 5
      time.from_until(timings[19], timings[20]) -> 56
      time.from_until(timings[21], timings[22]) -> 6
      else -> false
    }
  }

  fun correct_break(time: Time, time_period_index: Int): Boolean {
    val y = (time_period_index + 1) / 2.0 // \d.5 for lesson, \d.0 for break
    when (val ret = break_time(time)) {
      false -> return false
      is Int -> return when {
        ret < 10 -> (ret - 0.5 == y)
        else -> ((ret / 10).toDouble() == y)
      }
    }
    return false
  }

  fun get_start_date_of_current_semester(year: Int, month: Int): LocalDate {
    val first_monday_of_february = LocalDate.of(year, Month.FEBRUARY, 1)
      .with(
        TemporalAdjusters.dayOfWeekInMonth(
          1,
          DayOfWeek.MONDAY
        )
      ).dayOfMonth
    return when {
      month >= Month.SEPTEMBER.value -> {
        // 1st semester in the academic year
        LocalDate.of(year, Month.SEPTEMBER, 1)
      }
      else -> {
        // 2nd semester in the academic year
        // Get the beginning of the 2nd week of February ()
        when (first_monday_of_february) {
          1 -> LocalDate.of(year, Month.FEBRUARY, 8)
          else -> LocalDate.of(year, Month.FEBRUARY, first_monday_of_february)
        }
      }
    }
  }

  fun get_start_date_of_current_semester(calendar: Calendar): LocalDate {
    return get_start_date_of_current_semester(
      calendar[Calendar.YEAR],
      calendar[Calendar.MONTH] + 1
    )
  }

  /**
   * This is the only one method of getting week of year that works correctly.
   * @return value from interval [[1;53]]
   */
  fun get_week_of_year(year: Int, month: Int, day: Int): Int {
    val week_of_year = WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear()
    val date = LocalDate.of(year, month, day)
    return date.get(week_of_year)
  }

  fun get_week_of_year(date: LocalDate): Int {
    return get_week_of_year(date.year, date.monthValue, date.dayOfMonth)
  }

  fun get_week_of_year(calendar: Calendar): Int {
    return get_week_of_year(
      calendar[Calendar.YEAR],
      calendar[Calendar.MONTH] + 1,
      calendar[Calendar.DAY_OF_MONTH]
    )
  }

  fun set_calendar_date_for_today(calendar: Calendar) {
    val current_date = LocalDate.now()
    // Note: month count in Calendar starts with 0 for JANUARY
    calendar.set(
      current_date.year,
      current_date.monthValue - 1,
      current_date.dayOfMonth
    )
  }

  fun study_time(time: Time, y: Int = -1): Any {
    if (y == -1 || y == 0) {
      if (time.from_until(timings[0], timings[1])) return Pair(1, 1)
      else if (time.from_until(timings[2], timings[3])) return Pair(1, 2)
    }
    if (y == -1 || y == 2) {
      if (time.from_until(timings[4], timings[5])) return Pair(2, 1)
      else if (time.from_until(timings[6], timings[7])) return Pair(2, 2)
    }
    if (y == -1 || y == 4) {
      if (time.from_until(timings[8], timings[9])) return Pair(3, 1)
      else if (time.from_until(timings[10], timings[11])) return Pair(3, 2)
    }
    if (y == -1 || y == 6) {
      if (time.from_until(timings[12], timings[13])) return Pair(4, 1)
      else if (time.from_until(timings[14], timings[15])) return Pair(4, 2)
    }
    if (y == -1 || y == 8) {
      if (time.from_until(timings[16], timings[17])) return Pair(5, 1)
      else if (time.from_until(timings[18], timings[19])) return Pair(5, 2)
    }
    if (y == -1 || y == 10) {
      if (time.from_until(timings[20], timings[21])) return Pair(6, 1)
      else if (time.from_until(timings[22], timings[23])) return Pair(6, 2)
    }
    return false
  }
}
