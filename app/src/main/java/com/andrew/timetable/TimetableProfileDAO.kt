package com.andrew.timetable

import androidx.room.*

@Dao
interface TimetableProfileDAO {
  @Query("SELECT * FROM timetable_profiles")
  suspend fun get_all(): List<TimetableProfile>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(timetable_profile: TimetableProfile): Long

  @Update
  suspend fun update(timetable_profile: TimetableProfile)

  @Delete
  suspend fun delete(timetable_profile: TimetableProfile)

  @Query("DELETE FROM timetable_profiles WHERE name = :name")
  suspend fun delete(name: String)
}
