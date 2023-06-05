package com.andrew.timetable

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.gson.Gson
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {
  lateinit var app_settings: AppSettings
  lateinit var app_settingsDAO: AppSettingsDAO

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?,
  ) {
    lifecycleScope.launch {
      val db = DatabaseHelper.instance(requireContext())
      app_settingsDAO = db.app_settingsDAO()
      app_settings = app_settingsDAO.get()!!
      setPreferencesFromResource(R.xml.settings_screen, rootKey)
      val t = app_settings.timings
      mapOf(
        R.string.pref_time_since_lesson_started to t.time_since_lesson_started,
        R.string.pref_lessons_time_left to t.lessons_time_left,
        R.string.pref_time_until_next_lesson to t.time_until_next_lesson,
        R.string.pref_time_since_half_started to t.time_since_half_started,
        R.string.pref_halfs_time_left to t.halfs_time_left,
        R.string.pref_time_until_next_half to t.time_until_next_half,
      ).forEach { (str_res, checked) ->
        findPreference<SwitchPreferenceCompat>(
          getString(str_res)
        )!!.isChecked = checked
      }
    }
  }

  override fun onPreferenceTreeClick(preference: Preference): Boolean {
    val time_since_lesson_started =
      getString(R.string.pref_time_since_lesson_started)
    val lessons_time_left = getString(R.string.pref_lessons_time_left)
    val time_until_next_lesson = getString(R.string.pref_time_until_next_lesson)
    val time_since_half_started =
      getString(R.string.pref_time_since_half_started)
    val halfs_time_left = getString(R.string.pref_halfs_time_left)
    val time_until_next_half = getString(R.string.pref_time_until_next_half)

    when (preference.key) {
      time_since_lesson_started -> app_settings
        .timings.time_since_lesson_started =
        (preference as SwitchPreferenceCompat).isChecked
      lessons_time_left -> app_settings.timings.lessons_time_left =
        (preference as SwitchPreferenceCompat).isChecked
      time_until_next_lesson -> app_settings.timings.time_until_next_lesson =
        (preference as SwitchPreferenceCompat).isChecked
      time_since_half_started -> app_settings.timings.time_since_half_started =
        (preference as SwitchPreferenceCompat).isChecked
      halfs_time_left -> app_settings.timings.halfs_time_left =
        (preference as SwitchPreferenceCompat).isChecked
      time_until_next_half -> app_settings.timings.time_until_next_half =
        (preference as SwitchPreferenceCompat).isChecked
    }

    lifecycleScope.launch {
      app_settingsDAO.update(app_settings)
    }

    return super.onPreferenceTreeClick(preference)
  }
}