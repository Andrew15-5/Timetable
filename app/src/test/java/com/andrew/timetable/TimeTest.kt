package com.andrew.timetable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.InvalidParameterException

class TimeTest {
  fun seconds_from(hhmm: String): Long {
    return Time.from_hhmm(hhmm).to_seconds()
  }

  fun full_from(hhmm: String): String {
    return Time.from_hhmm(hhmm).full_format()
  }

  fun short_from(hhmm: String): String {
    return Time.from_hhmm(hhmm).short_format()
  }

  @Test
  fun test_throw_InvalidParameterException_when_instantiating_from_hhmm() {
    for (time in arrayOf(
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
    )) {
      assertThrows(InvalidParameterException::class.java) {
        Time.from_hhmm(time)
      }
    }
  }

  @Test
  fun test_to_seconds() {
    for (time in arrayOf("0:0", "0:00", "00:0", "00:00")) {
      assertEquals(seconds_from(time), 0)
    }
    assertEquals(60, seconds_from("0:1"))
    assertEquals(60, seconds_from("0:01"))
    assertEquals(60 * 10, seconds_from("0:10"))
    assertEquals(60 * 30, seconds_from("0:30"))
    assertEquals(60 * 59, seconds_from("0:59"))
    assertEquals(60 * 60, seconds_from("0:60"))
    assertEquals(60 * 99, seconds_from("0:99"))
    assertEquals(3600, seconds_from("1:0"))
    assertEquals(3600, seconds_from("1:00"))
    assertEquals(3600 + 60, seconds_from("1:01"))
    assertEquals(3600 + 60 * 10, seconds_from("1:10"))
    assertEquals(3600 + 60 * 11, seconds_from("1:11"))
    assertEquals(3600 * 3 + 60 * 54, seconds_from("3:54"))
    assertEquals(3600 * 15 + 60 * 31, seconds_from("15:31"))
    assertEquals(3600 * 23 + 60 * 59, seconds_from("23:59"))
    assertEquals(0, seconds_from("23:60"))
    assertEquals(60 * 59, seconds_from("24:59"))
    assertEquals(60 * 60, seconds_from("24:60"))
    assertEquals(3600 * 4 + 60 * 39, seconds_from("99:99"))
  }

  @Test
  fun test_full_format() {
    for (time in arrayOf("0:0", "0:00", "00:0", "00:00")) {
      assertEquals(full_from(time), "00:00:00")
    }
    assertEquals("00:01:00", full_from("0:1"))
    assertEquals("00:01:00", full_from("0:01"))
    assertEquals("00:10:00", full_from("0:10"))
    assertEquals("00:30:00", full_from("0:30"))
    assertEquals("00:59:00", full_from("0:59"))
    assertEquals("01:00:00", full_from("0:60"))
    assertEquals("01:39:00", full_from("0:99"))
    assertEquals("01:00:00", full_from("1:0"))
    assertEquals("01:00:00", full_from("1:00"))
    assertEquals("01:01:00", full_from("1:01"))
    assertEquals("01:10:00", full_from("1:10"))
    assertEquals("01:11:00", full_from("1:11"))
    assertEquals("03:54:00", full_from("3:54"))
    assertEquals("15:31:00", full_from("15:31"))
    assertEquals("23:59:00", full_from("23:59"))
    assertEquals("00:00:00", full_from("23:60"))
    assertEquals("00:59:00", full_from("24:59"))
    assertEquals("01:00:00", full_from("24:60"))
    assertEquals("04:39:00", full_from("99:99"))
  }

  @Test
  fun test_short_format() {
    for (time in arrayOf("0:0", "0:00", "00:0", "00:00")) {
      assertEquals(short_from(time), "00:00")
    }
    assertEquals("01:00", short_from("0:1"))
    assertEquals("01:00", short_from("0:01"))
    assertEquals("10:00", short_from("0:10"))
    assertEquals("30:00", short_from("0:30"))
    assertEquals("59:00", short_from("0:59"))
    assertEquals("01:00:00", short_from("0:60"))
    assertEquals("01:39:00", short_from("0:99"))
    assertEquals("01:00:00", short_from("1:0"))
    assertEquals("01:00:00", short_from("1:00"))
    assertEquals("01:01:00", short_from("1:01"))
    assertEquals("01:10:00", short_from("1:10"))
    assertEquals("01:11:00", short_from("1:11"))
    assertEquals("03:54:00", short_from("3:54"))
    assertEquals("15:31:00", short_from("15:31"))
    assertEquals("23:59:00", short_from("23:59"))
    assertEquals("00:00", short_from("23:60"))
    assertEquals("59:00", short_from("24:59"))
    assertEquals("01:00:00", short_from("24:60"))
    assertEquals("04:39:00", short_from("99:99"))
  }
}
