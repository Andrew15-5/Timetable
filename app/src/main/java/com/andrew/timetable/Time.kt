package com.andrew.timetable

import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.security.InvalidParameterException

class Time {

  private constructor(duration: Duration) {
    time = Duration.ofSeconds(duration.seconds % Duration.ofDays(1).seconds)
  }

  private val time: Duration

  companion object {
    /**
     * Converts formatted string of hours and minutes to seconds
     * @param hhmm string with "RegEx" format: H?H:M?M
     * (H - hour \d, S - minute \d) (mod 24 hours)
     * @throws InvalidParameterException if hhmm parameter has invalid format
     */
    fun from_hhmm(hhmm: String): Time {
      if (!Regex("^\\d{1,2}:\\d{1,2}$").matches(hhmm))
        throw InvalidParameterException("String has invalid format")
      return Time(Duration.parse("PT${hhmm.replace(':', 'H')}M"))
    }

    /**
     * Use current time of the day
     */
    fun now(): Time {
      val now_time = LocalTime.now()
      val h = now_time.hour
      val m = now_time.minute
      val s = now_time.second
      return Time(Duration.parse("PT${h}H${m}M${s}S"))
    }
  }

  private fun format(pattern: String): String {
    return LocalTime.ofSecondOfDay(time.seconds)
      .format(DateTimeFormatter.ofPattern(pattern))
  }

  /**
   * Checks if from <= time < until
   */
  fun from_until(from: Time, until: Time): Boolean {
    return from <= this && this < until
  }

  fun full_format(): String {
    return format("HH:mm:ss")
  }

  fun short_format(): String {
    return when {
      time.toHours() > 0 -> format("H:mm:ss")
      else -> format("mm:ss")
    }
  }

  fun to_seconds(): Long {
    return time.seconds
  }

  operator fun compareTo(other: Time): Int {
    return time.compareTo(other.time)
  }

  override operator fun equals(other: Any?): Boolean {
    return other is Time? && time == other?.time
  }

  override fun hashCode(): Int {
    return time.hashCode()
  }

  operator fun minus(other: Time): Time {
    return Time(time - other.time)
  }

  operator fun plus(other: Time): Time {
    return Time(time + other.time)
  }
}
