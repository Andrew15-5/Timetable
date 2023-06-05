package com.andrew.timetable

import androidx.room.RoomDatabase
import androidx.room.Database as DatabaseAnnotation

@DatabaseAnnotation(
  entities = [AppSettings::class, Profile::class],
  version = 1,
  exportSchema = false,
)
abstract class Database : RoomDatabase() {
  abstract fun profileDAO(): ProfileDAO
  abstract fun app_settingsDAO(): AppSettingsDAO
}
