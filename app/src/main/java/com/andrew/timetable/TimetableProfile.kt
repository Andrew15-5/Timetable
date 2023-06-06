package com.andrew.timetable

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import org.json.JSONObject

@Entity(tableName = "timetable_profiles")
@TypeConverters(JSONObjectTypeConverter::class)
data class TimetableProfile(
  @PrimaryKey
  val name: String,
  val timetable: JSONObject,
) {
  companion object {
    fun fromJSON(json: String): TimetableProfile {
      val gson = Gson()
      val map = gson.fromJson(json, Map::class.java)
      return TimetableProfile(
        map["name"] as String,
        JSONObject(gson.toJson(map["timetable"]))
      )
    }
  }

  fun toJSON(): String {
    return Gson().toJson(
      mapOf(
        "name" to name,
        "timetable" to Gson().fromJson(timetable.toString(), Map::class.java)
      )
    )
  }

  fun pretty_timetable(): String? {
    return GsonBuilder().setPrettyPrinting().create()
      .toJson(Gson().fromJson(timetable.toString(), Map::class.java))
  }
}


class JSONObjectTypeConverter {
  @TypeConverter
  fun fromJSONObject(timetable: JSONObject): String {
    return timetable.toString()
  }

  @TypeConverter
  fun toJSONObject(timetable_json: String): JSONObject {
    return JSONObject(timetable_json)
  }
}

