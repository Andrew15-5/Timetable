package com.andrew.timetable

import androidx.room.*

@Dao
interface ProfileDAO {
  @Query("SELECT * FROM profiles")
  suspend fun get_all(): List<Profile>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert_all(profiles: List<Profile>): List<Long>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert_all_and_update_id(profiles: List<Profile>): List<Long> {
    val ids = insert_all(profiles)
    for ((i, id) in ids.withIndex()) profiles[i].id = id
    return ids
  }

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert_all(vararg profiles: Profile): List<Long>

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert_all_and_update_id(vararg profiles: Profile): List<Long> {
    return insert_all_and_update_id((profiles).toList())
  }

  @Update
  suspend fun update_all(profiles: List<Profile>): Int

  @Update
  suspend fun update_all(vararg profiles: Profile): Int

  @Delete
  suspend fun delete(profile: Profile)

  @Delete
  suspend fun delete_all(profiles: List<Profile>)

  @Delete
  suspend fun delete_all(vararg profiles: Profile)

  @Delete
  suspend fun delete_all() = delete_all(get_all())
}
