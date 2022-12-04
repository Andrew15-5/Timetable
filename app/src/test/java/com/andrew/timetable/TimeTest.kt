package com.andrew.timetable

import org.junit.Assert.*
import org.junit.Test
import java.security.InvalidParameterException

class TimeTest {
  @Test
  fun test_throw_InvalidParameterException_when_instantiating_from_hhmm() {
    arrayOf(
      "",
      ":",
      "0000",
      "0:",
      ":0",
      "00:",
      ":00",
      "000:00",
      "00:000",
      "a0:00",
      "0a:00",
      "00:a0",
      "00:0a"
    ).forEach { time ->
      assertThrows(InvalidParameterException::class.java) {
        Time.from_hhmm(time)
      }
    }
  }

  @Test
  fun test_to_seconds() {
    arrayOf("0:0", "0:00", "00:0", "00:00").forEach { hhmm ->
      assertEquals(0, Time.from_hhmm(hhmm).to_seconds())
    }
    mapOf(
      60 to "0:1",
      60 to "0:01",
      60 * 10 to "0:10",
      60 * 30 to "0:30",
      60 * 59 to "0:59",
      60 * 60 to "0:60",
      60 * 99 to "0:99",
      3600 to "1:0",
      3600 to "1:00",
      3600 + 60 to "1:01",
      3600 + 60 * 10 to "1:10",
      3600 + 60 * 11 to "1:11",
      3600 * 3 + 60 * 54 to "3:54",
      3600 * 15 + 60 * 31 to "15:31",
      3600 * 23 + 60 * 59 to "23:59",
      0 to "23:60",
      60 * 59 to "24:59",
      60 * 60 to "24:60",
      3600 * 4 + 60 * 39 to "99:99",
    ).forEach { (x, y) ->
      assertEquals(x.toLong(), Time.from_hhmm(y).to_seconds())
    }
  }

  @Test
  fun test_full_format() {
    arrayOf("0:0", "0:00", "00:0", "00:00").forEach { hhmm ->
      assertEquals("00:00:00", Time.from_hhmm(hhmm).full_format())
    }
    mapOf(
      "00:01:00" to "0:1",
      "00:01:00" to "0:01",
      "00:10:00" to "0:10",
      "00:30:00" to "0:30",
      "00:59:00" to "0:59",
      "01:00:00" to "0:60",
      "01:39:00" to "0:99",
      "01:00:00" to "1:0",
      "01:00:00" to "1:00",
      "01:01:00" to "1:01",
      "01:10:00" to "1:10",
      "01:11:00" to "1:11",
      "03:54:00" to "3:54",
      "15:31:00" to "15:31",
      "23:59:00" to "23:59",
      "00:00:00" to "23:60",
      "00:59:00" to "24:59",
      "01:00:00" to "24:60",
      "04:39:00" to "99:99",
    ).forEach { (x, y) -> assertEquals(x, Time.from_hhmm(y).full_format()) }
  }

  @Test
  fun test_short_format() {
    val prefix = "   "
    arrayOf("0:0", "0:00", "00:0", "00:00").forEach { hhmm ->
      assertEquals(prefix + "00:00", Time.from_hhmm(hhmm).short_format())
    }
    mapOf(
      "01:00" to "0:1",
      "01:00" to "0:01",
      "10:00" to "0:10",
      "30:00" to "0:30",
      "59:00" to "0:59",
      "1:00:00" to "0:60",
      "1:39:00" to "0:99",
      "1:00:00" to "1:0",
      "1:00:00" to "1:00",
      "1:01:00" to "1:01",
      "1:10:00" to "1:10",
      "1:11:00" to "1:11",
      "3:54:00" to "3:54",
      "15:31:00" to "15:31",
      "23:59:00" to "23:59",
      "00:00" to "23:60",
      "59:00" to "24:59",
      "1:00:00" to "24:60",
      "4:39:00" to "99:99",
    ).forEach { (x, y) ->
      val expected = if (x.length <= 5) prefix + x else x
      assertEquals(expected, Time.from_hhmm(y).short_format())
    }
  }

  @Test
  fun test_less_than() {
    mapOf(
      "0:00" to "0:01",
      "1:00" to "1:01",
      "1:00" to "2:00",
      "23:58" to "23:59",
    ).forEach { (x, y) -> assertTrue(Time.from_hhmm(x) < Time.from_hhmm(y)) }
  }

  @Test
  fun test_greater_than() {
    mapOf(
      "0:00" to "0:01",
      "1:00" to "1:01",
      "1:00" to "2:00",
      "23:58" to "23:59",
    ).forEach { (x, y) -> assertTrue(Time.from_hhmm(y) > Time.from_hhmm(x)) }
  }

  @Test
  fun test_equal_to() {
    arrayOf(
      "0:00",
      "0:01",
      "1:00",
      "12:00",
      "23:59",
    ).forEach { hhmm ->
      assertTrue(Time.from_hhmm(hhmm) == Time.from_hhmm(hhmm))
    }
  }
}
