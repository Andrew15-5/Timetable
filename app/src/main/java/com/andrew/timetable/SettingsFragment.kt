package com.andrew.timetable

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.andrew.timetable.MainActivity.Companion.BROADCAST_ACTION_APP_SETTINGS_UPDATED
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {
  lateinit var app_settingsDAO: AppSettingsDAO

  val broadcast_receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == BROADCAST_ACTION_APP_SETTINGS_UPDATED) {
        sync_preferences()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val intent_filter = IntentFilter(BROADCAST_ACTION_APP_SETTINGS_UPDATED)
    LocalBroadcastManager.getInstance(requireContext())
      .registerReceiver(broadcast_receiver, intent_filter)
  }

  override fun onPause() {
    LocalBroadcastManager.getInstance(requireContext())
      .unregisterReceiver(broadcast_receiver)
    super.onPause()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity as MainActivity).settings_menu_item_visibility(false)
  }

  override fun onDetach() {
    super.onDetach()
    (activity as MainActivity).settings_menu_item_visibility(true)
  }

  fun sync_preferences() {
    lifecycleScope.launch {
      val db = DatabaseHelper.instance(requireContext())
      app_settingsDAO = db.app_settingsDAO()
      val app_settings = app_settingsDAO.get()!!
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

  fun init_custom_preferences() {
    val activity = activity as MainActivity
    findPreference<Preference>(
      getString(R.string.pref_restore_default_settings)
    )!!.setOnPreferenceClickListener {
      AlertDialog
        .Builder(activity, R.style.Theme_Timetable_AlertDialog)
        .setTitle(
          "Are you sure? You should create a backup of your current settings"
        )
        .setPositiveButton("Yes") { _, _ ->
          activity.deleteDatabase(getString(R.string.database_name))
          DatabaseHelper.close()
          lifecycleScope.launch {
            DatabaseHelper.instance(activity)
            LocalBroadcastManager
              .getInstance(activity)
              .sendBroadcast(Intent(BROADCAST_ACTION_APP_SETTINGS_UPDATED))
            Snackbar.make(
              activity,
              this@SettingsFragment.requireView(),
              "Settings set to default",
              Snackbar.LENGTH_SHORT
            ).show()
          }
        }
        .setNegativeButton("I'm not sure") { dialog, _ ->
          dialog.cancel()
        }
        .create()
        .show()
      true
    }
  }

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?,
  ) {
    setPreferencesFromResource(R.xml.settings_screen, rootKey)
    init_custom_preferences()
    sync_preferences()
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

    lifecycleScope.launch {
      val app_settings = app_settingsDAO.get()!!

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

      app_settingsDAO.update(app_settings)
    }

    return super.onPreferenceTreeClick(preference)
  }
}