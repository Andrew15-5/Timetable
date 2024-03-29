package com.andrew.timetable

import android.content.res.AssetManager
import org.json.JSONObject

class TimetableConfigs(
  assets: AssetManager,
  config_names: Array<String>,
  initial_config_index: Int = 0,
) {
  enum class Config {
    CURRENT, NEXT
  }

  private var current_config = initial_config_index
  val configs = mutableListOf<JSONObject>()
  val config_names = mutableListOf<String>()

  init {
    for (config_name in config_names) {
      assets.open(config_name).use {
        it.bufferedReader().use { buffered_reader ->
          val config = buffered_reader.readText()
          configs.add(JSONObject(config))
        }
      }
    }
  }

  fun get_current_config_name(): String? {
    if (isEmpty()) return null
    return config_names[current_config]
  }

  fun get_current_config(): JSONObject? {
    if (isEmpty()) return null
    return configs[current_config]
  }

  fun get_next_config(): JSONObject? {
    current_config = (current_config + 1) % configs.size
    return get_current_config()
  }

  fun get_config(which_one: Config): JSONObject? {
    return when {
      which_one === Config.NEXT -> get_next_config()
      else -> get_current_config()
    }
  }
}

fun TimetableConfigs.isEmpty(): Boolean {
  return configs.isEmpty()
}
