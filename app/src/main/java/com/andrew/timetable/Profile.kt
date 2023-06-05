package com.andrew.timetable

import androidx.room.*
import com.google.gson.Gson

@Entity(
  tableName = "profiles",
  indices = [Index(value = ["name"], unique = true)]
)
@TypeConverters(TimingsTypeConverter::class)
data class Profile(
  @ColumnInfo(name = "name")
  val name: String,

  @ColumnInfo(name = "timings")
  val timings: Timings,
) {
  @PrimaryKey(autoGenerate = true)
  var id: Long = 0
}

class Timings(
  val time_since_half_started_text_view: Boolean,
  val halfs_time_left_text_view: Boolean,
  val time_until_next_half_text_view: Boolean,
  val time_since_lesson_started_text_view: Boolean,
  val lessons_time_left_text_view: Boolean,
  val time_until_next_lesson_text_view: Boolean,
) {
}

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
