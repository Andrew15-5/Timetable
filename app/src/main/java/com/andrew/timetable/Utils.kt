package com.andrew.timetable

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.*

class Utils {
  val no_time = "--:--"

  // Each line has 4 timings and represent nth lesson ([1;6])
  private val timings_str = arrayOf(
    "8:30", "9:15", "9:20", "10:05",
    "10:20", "11:05", "11:10", "11:55",
    "12:10", "12:55", "13:00", "13:45",
    "14:15", "15:00", "15:05", "15:50",
    "16:05", "16:50", "16:55", "17:40",
    "17:50", "18:35", "18:40", "19:25",
    "19:35", "20:20", "20:25", "21:10",
  )
  val timings = timings_str.map { time -> Time.from_hhmm(time) }
  val time_periods: List<String>

  init {
    val mutable_time_periods = mutableListOf<String>()
    val group_by = 4 // Each lesson has 4 timings
    for (i in timings_str.indices step group_by) {
      val start = timings_str[i]
      val recess_start = timings_str[i + 1]
      val recess_end = timings_str[i + 2]
      val end = timings_str[i + 3]
      mutable_time_periods += "$start-$recess_start | $recess_end-$end"
      if (i + 4 >= timings_str.size) continue
      val next_start = timings_str[i + 4]
      mutable_time_periods += "$end-$next_start"
    }
    time_periods = mutable_time_periods.toList()
  }

  fun get_time_since_half_started(time: Time): String {
    val timing_index = get_lesson(time)?.timing_index ?: return no_time
    val time_difference = time - timings[timing_index]
    return time_difference.short_format()
  }

  fun get_half_time_left(time: Time): String {
    val timing_index = get_lesson(time)?.timing_index ?: return no_time
    val time_difference = timings[timing_index + 1] - time
    return time_difference.short_format()
  }

  fun get_time_until_next_half(time: Time): String {
    val first_half_start_time = timings[0]
    val last_half_start_time = timings.reversed()[1]
    val lesson = get_lesson(time)
    val recess = get_recess(time)
    val subtract_from = when {
      time < first_half_start_time -> first_half_start_time
      time >= last_half_start_time -> return no_time
      lesson != null -> timings[lesson.timing_index + 2]
      else -> timings[recess!!.timing_index + 1]
    }
    val time_difference = subtract_from - time
    return time_difference.short_format()
  }

  fun get_time_since_lesson_started(time: Time): String {
    val lesson = get_lesson(time)
    val recess = get_recess(time)
    if (
      (lesson == null && recess == null) ||
      (recess != null && recess.is_between_lessons)
    ) return no_time
    val subtract = when {
      recess != null && recess.is_during_lesson -> {
        timings[recess.timing_index - 1]
      }
      else -> when (lesson!!.half) {
        1 -> timings[lesson.timing_index]
        else -> timings[lesson.timing_index - 2]
      }
    }
    val time_difference = time - subtract
    return time_difference.short_format()
  }

  fun get_lessons_time_left(time: Time): String {
    val lesson = get_lesson(time)
    val recess = get_recess(time)
    if (
      (lesson == null && recess == null) ||
      (recess != null && recess.is_between_lessons)
    ) return no_time
    val subtract_from = when {
      recess != null && recess.is_during_lesson -> {
        timings[recess.timing_index + 2]
      }
      else -> when (lesson!!.half) {
        1 -> timings[lesson.timing_index + 3]
        else -> timings[lesson.timing_index + 1]
      }
    }
    val time_difference = subtract_from - time
    return time_difference.short_format()
  }

  fun get_time_until_next_lesson(time: Time): String {
    val first_lesson_start_time = timings[0]
    val last_lesson_start_time = timings.reversed()[3]
    val lesson = get_lesson(time)
    val recess = get_recess(time)
    val subtract_from = when {
      time < first_lesson_start_time -> first_lesson_start_time
      time >= last_lesson_start_time -> return no_time
      lesson != null -> when (lesson.half) {
        1 -> timings[lesson.timing_index + 4]
        else -> timings[lesson.timing_index + 2]
      }
      else -> when {
        recess!!.is_during_lesson -> timings[recess.timing_index + 3]
        else -> timings[recess.timing_index + 1]
      }
    }
    val time_difference = subtract_from - time
    return time_difference.short_format()
  }

  fun get_recess(time: Time): Recess? {
    // return when {
    //   time.from_until(timings[1], timings[2]) -> Recess.during(1)
    //   time.from_until(timings[3], timings[4]) -> Recess.after(1)
    //   time.from_until(timings[5], timings[6]) -> Recess.during(2)
    //   time.from_until(timings[7], timings[8]) -> Recess.after(2)
    //   time.from_until(timings[9], timings[10]) -> Recess.during(3)
    //   time.from_until(timings[11], timings[12]) -> Recess.after(3)
    //   ...
    //   else -> null
    // }
    val length = timings.size
    for (i in 1 until length step 4) {
      if (i + 1 > length - 1) break
      if (time.from_until(timings[i], timings[i + 1])) {
        return Recess.during(i / 4 + 1)
      }
      if (i + 3 > length - 1) break
      if (time.from_until(timings[i + 2], timings[i + 3])) {
        return Recess.after(i / 4 + 1)
      }
    }
    return null
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

  fun get_lesson(time: Time): Lesson? {
    // return when {
    //   time.from_until(timings[0], timings[1]) -> Lesson(1, 1)
    //   time.from_until(timings[2], timings[3]) -> Lesson(1, 2)
    //   time.from_until(timings[4], timings[5]) -> Lesson(2, 1)
    //   time.from_until(timings[6], timings[7]) -> Lesson(2, 2)
    //   time.from_until(timings[8], timings[9]) -> Lesson(3, 1)
    //   time.from_until(timings[10], timings[11]) -> Lesson(3, 2)
    //   ...
    //   else -> null
    // }
    val length = timings.size
    for (i in 0 until length step 4) {
      val lesson = i / 4 + 1
      if (i + 1 > length - 1) break
      var half = 1
      if (time.from_until(timings[i], timings[i + 1])) {
        return Lesson(lesson, half)
      }
      if (i + 3 > length - 1) break
      half = 2
      if (time.from_until(timings[i + 2], timings[i + 3])) {
        return Lesson(lesson, half)
      }
    }
    return null
  }
}
