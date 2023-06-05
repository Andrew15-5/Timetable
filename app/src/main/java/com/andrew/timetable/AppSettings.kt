package com.andrew.timetable

import androidx.room.*
import com.google.gson.Gson

@Entity(tableName = "app_settings")
@TypeConverters(TimingsTypeConverter::class)
data class AppSettings(
  val timings: Timings,
) {
  @PrimaryKey
  var id: Int = 0
}

class Timings(
  var time_since_lesson_started: Boolean,
  var lessons_time_left: Boolean,
  var time_until_next_lesson: Boolean,
  var time_since_half_started: Boolean,
  var halfs_time_left: Boolean,
  var time_until_next_half: Boolean,
)

class TimingsTypeConverter {
  private val gson = Gson()

  @TypeConverter
  fun fromTimings(timings: Timings): String {
    return gson.toJson(timings)
  }

  @TypeConverter
  fun toTimings(timingsJson: String): Timings {
    return gson.fromJson(timingsJson, Timings::class.java)
  }
}
