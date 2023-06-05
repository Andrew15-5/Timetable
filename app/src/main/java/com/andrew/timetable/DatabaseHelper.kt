package com.andrew.timetable

import android.content.Context
import androidx.room.Room

class DatabaseHelper {
  companion object {
    private var db: Database? = null

    suspend fun instance(context: Context): Database {
      if (db != null) return db as Database
      db = Room.databaseBuilder(
        context,
        Database::class.java,
        context.getString(R.string.database_name)
      )
        .fallbackToDestructiveMigration()
        .build()
      if (!is_initialized()) init()
      return db as Database
    }

    private suspend fun is_initialized(): Boolean {
      return db!!.app_settingsDAO().get() != null
    }

    private suspend fun init() {
      val timings = Timings(
        time_since_half_started = true,
        halfs_time_left = true,
        time_until_next_half = true,
        time_since_lesson_started = true,
        lessons_time_left = true,
        time_until_next_lesson = true,
      )
      val app_settings = AppSettings(timings)
      db!!.app_settingsDAO().insert(app_settings)
    }
  }
}
