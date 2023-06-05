package com.andrew.timetable

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Profile::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
  abstract fun profileDAO(): ProfileDAO
}