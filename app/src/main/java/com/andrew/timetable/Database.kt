package com.andrew.timetable

import androidx.room.RoomDatabase
import androidx.room.Database as DatabaseAnnotation

@DatabaseAnnotation(
  entities = [AppSettings::class, TimetableProfile::class],
  version = 1,
  exportSchema = false,
)
abstract class Database : RoomDatabase() {
  abstract fun timetable_profileDAO(): TimetableProfileDAO
  abstract fun app_settingsDAO(): AppSettingsDAO
}
