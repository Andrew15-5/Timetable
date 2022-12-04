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
    "16:05", "16:50", "16:55", "17:40",
    "17:50", "18:35", "18:40", "19:25"
  )
  val timings = timings_str.map { time -> Time.from_hhmm(time) }
  val time_periods: List<String>

  init {
    val mutable_time_periods = mutableListOf<String>()
    val group_by = 4 // Each pair has 4 timings
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

  fun get_lesson_time_left(time: Time): String {
    val timing_index = get_pair(time)?.timing_index
    val study_time = timing_index != null
    return when {
      study_time -> timings[timing_index!! + 1].minus(time).short_format()
      else -> "   --:--  "
    }
  }

  fun get_time_until_next_lesson(time: Time): String {
    val last_recess_end_time = timings.reversed()[1]
    val pair_index = get_pair(time)?.timing_index
    val recess_index = get_recess(time)?.timing_index
    val start_time = timings[0]
    val study_time = pair_index != null
    val other_time = when {
      time < start_time -> start_time
      time >= last_recess_end_time -> return "     --:--"
      study_time -> timings[pair_index!! + 2]
      else -> timings[recess_index!! + 1]
    }
    return other_time.minus(time).short_format()
  }

  fun get_pairs_time_left(time: Time): String {
    val pair = get_pair(time)
    val pair_index = pair?.timing_index
    val recess = get_recess(time)
    val recess_index = recess?.timing_index
    val recess_time = recess != null
    val timing_index = pair_index ?: recess_index
    val not_study_or_recess_time = timing_index == null
    val other_time = when {
      not_study_or_recess_time || recess_time && recess!!.is_between_pairs -> {
        return "      --:--  "
      }
      recess_time && recess!!.is_during_pair -> timings[recess_index!! + 2]
      else -> when (pair!!.lesson) {
        1 -> timings[pair_index!! + 3]
        else -> timings[pair_index!! + 1]
      }
    }
    return other_time.minus(time).short_format()
  }

  fun get_time_until_next_pair(time: Time): String {
    val last_recess_between_pairs_end = timings.reversed()[3]
    val pair = get_pair(time)
    val pair_index = pair?.timing_index
    val recess = get_recess(time)
    val recess_index = recess?.timing_index
    val start_time = timings[0]
    val study_time = pair != null
    val other_time = when {
      time < start_time -> start_time
      time >= last_recess_between_pairs_end -> return "     --:--"
      study_time -> when (pair!!.lesson) {
        1 -> timings[pair_index!! + 4]
        else -> timings[pair_index!! + 2]
      }
      else -> when {
        recess!!.is_during_pair -> timings[recess_index!! + 3]
        else -> timings[recess_index!! + 1]
      }
    }
    return other_time.minus(time).short_format()
  }

  fun get_recess(time: Time): Recess? {
    return when {
      time.from_until(timings[1], timings[2]) -> Recess.during(1)
      time.from_until(timings[3], timings[4]) -> Recess.after(1)
      time.from_until(timings[5], timings[6]) -> Recess.during(2)
      time.from_until(timings[7], timings[8]) -> Recess.after(2)
      time.from_until(timings[9], timings[10]) -> Recess.during(3)
      time.from_until(timings[11], timings[12]) -> Recess.after(3)
      time.from_until(timings[13], timings[14]) -> Recess.during(4)
      time.from_until(timings[15], timings[16]) -> Recess.after(4)
      time.from_until(timings[17], timings[18]) -> Recess.during(5)
      time.from_until(timings[19], timings[20]) -> Recess.after(5)
      time.from_until(timings[21], timings[22]) -> Recess.during(6)
      else -> null
    }
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

  fun get_pair(time: Time): Pair? {
    return when {
      time.from_until(timings[0], timings[1]) -> Pair(1, 1)
      time.from_until(timings[2], timings[3]) -> Pair(1, 2)
      time.from_until(timings[4], timings[5]) -> Pair(2, 1)
      time.from_until(timings[6], timings[7]) -> Pair(2, 2)
      time.from_until(timings[8], timings[9]) -> Pair(3, 1)
      time.from_until(timings[10], timings[11]) -> Pair(3, 2)
      time.from_until(timings[12], timings[13]) -> Pair(4, 1)
      time.from_until(timings[14], timings[15]) -> Pair(4, 2)
      time.from_until(timings[16], timings[17]) -> Pair(5, 1)
      time.from_until(timings[18], timings[19]) -> Pair(5, 2)
      time.from_until(timings[20], timings[21]) -> Pair(6, 1)
      time.from_until(timings[22], timings[23]) -> Pair(6, 2)
      else -> null
    }
  }
}
