package com.andrew.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
  @PrimaryKey
  val name: String,
)
