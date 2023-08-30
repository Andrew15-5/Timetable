package com.andrew.timetable

import androidx.room.*

@Dao
interface AppSettingsDAO {
  @Query("SELECT * FROM app_settings")
  suspend fun get_all(): List<AppSettings>

  suspend fun get(): AppSettings? {
    val list = get_all()
    return if (list.isEmpty()) null else list[0]
  }

  @Insert
  suspend fun insert(app_settings: AppSettings)

  @Update
  suspend fun update(app_settings: AppSettings)

  @Delete
  suspend fun delete(app_settings: AppSettings)
}
