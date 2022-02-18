package com.andrew.timetable

import android.content.res.AssetManager
import com.andrew.timetable.TimetableConfigs.Config.CURRENT
import com.andrew.timetable.TimetableConfigs.Config.NEXT
import org.json.JSONObject

class TimetableConfigs(
  assets: AssetManager,
  config_names: Array<String>,
  initial_config_index: Int = 0
) {
  enum class Config {
    CURRENT, NEXT
  }

  private var current_config_m = initial_config_index
  private val timetable_configs_m = mutableListOf<JSONObject>()

  init {
    for (config_name in config_names.withIndex()) {
      val config = assets.open(config_name.value).bufferedReader().use { it.readText() }
      timetable_configs_m.add(JSONObject(config))
    }
  }

  fun current_config(): JSONObject {
    return timetable_configs_m[current_config_m]
  }

  fun next_config(): JSONObject {
    current_config_m = (current_config_m + 1) % timetable_configs_m.size
    return current_config()
  }

  fun config(which_one: Config): JSONObject {
    var return_value = current_config()
    if (which_one === CURRENT) return_value = current_config()
    if (which_one === NEXT) return_value = next_config()
    return return_value
  }
}
