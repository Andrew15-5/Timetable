package com.andrew.timetable

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.andrew.timetable.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import com.andrew.timetable.Snackbar.Companion as ThemedSnackbar

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var nav_controller: NavController
  private lateinit var app_bar_configuration: AppBarConfiguration
  private lateinit var fragment_manager: FragmentManager
  private val current_fragment
    get() = fragment_manager.primaryNavigationFragment
  private lateinit var backup_dir: File
  private var menu: Menu? = null

  companion object {
    const val BROADCAST_ACTION_APP_SETTINGS_UPDATED =
      "${BuildConfig.APPLICATION_ID}.broadcast.app_settings_updated"
  }

  private suspend fun read_text(uri: Uri): String? {
    val parcel_file_descriptor =
      contentResolver.openFileDescriptor(uri, "r", null)
    if (parcel_file_descriptor == null) {
      make_snackbar("Couldn't open file", Snackbar.LENGTH_LONG).show()
      return null
    }
    val file_descriptor = parcel_file_descriptor.fileDescriptor
    val file_input_stream = FileInputStream(file_descriptor)
    val buffered_reader = BufferedReader(InputStreamReader(file_input_stream))
    val string_builder = StringBuilder()
    var line: String?

    do {
      line = withContext(Dispatchers.IO) {
        buffered_reader.readLine()
      }
      if (line == null) break
      if (string_builder.isNotEmpty()) string_builder.append('\n')
      string_builder.append(line)
    } while (true)
    withContext(Dispatchers.IO) {
      buffered_reader.close()
      file_input_stream.close()
    }
    parcel_file_descriptor.close()
    return string_builder.toString()
  }

  private val timetable_profile_picker_launcher = registerForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri == null) {
      make_snackbar("No profile was selected", Snackbar.LENGTH_LONG).show()
      return@registerForActivityResult
    }
    lifecycleScope.launch {
      val imported_profile = read_text(uri) ?: return@launch

      val db = DatabaseHelper.instance(this@MainActivity)
      val profile = TimetableProfile.fromJSON(imported_profile)
      val result = db.timetable_profileDAO().insert(profile)
      if (result == -1L) {
        make_snackbar(
          "Profile ${profile.name} already exists",
          Snackbar.LENGTH_SHORT
        ).show()
        return@launch
      }

      LocalBroadcastManager
        .getInstance(this@MainActivity)
        .sendBroadcast(Intent(BROADCAST_ACTION_APP_SETTINGS_UPDATED))

      make_snackbar("Profile imported", Snackbar.LENGTH_SHORT).show()
    }
  }

  private val backup_file_picker_launcher = registerForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri == null) {
      make_snackbar("No file was selected", Snackbar.LENGTH_LONG).show()
      return@registerForActivityResult
    }

    val snackbar = make_snackbar("Settings restored", Snackbar.LENGTH_SHORT)

    lifecycleScope.launch {
      val restored_text = read_text(uri) ?: return@launch

      val db = DatabaseHelper.instance(this@MainActivity)
      val app_settings = Gson().fromJson(restored_text, AppSettings::class.java)
      db.app_settingsDAO().update(app_settings)

      LocalBroadcastManager
        .getInstance(this@MainActivity)
        .sendBroadcast(Intent(BROADCAST_ACTION_APP_SETTINGS_UPDATED))

      snackbar.show()
    }
  }

  @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val downloads_dir = File(
      Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
      ).toURI()
    )
    val app_name = applicationInfo.loadLabel(packageManager).toString()
    backup_dir = File(downloads_dir, "$app_name backups")

    lifecycleScope.launch {
      // Create instance from the very beginning to not waste time later
      DatabaseHelper.instance(this@MainActivity)

      binding = ActivityMainBinding.inflate(layoutInflater)
      setContentView(binding.root)

      setSupportActionBar(binding.toolbar)
      fragment_manager = supportFragmentManager.findFragmentById(
        R.id.navigationContainer
      )?.childFragmentManager!!

      nav_controller = findNavController(R.id.navigationContainer)
      app_bar_configuration = AppBarConfiguration(nav_controller.graph)
      setupActionBarWithNavController(nav_controller, app_bar_configuration)
    }
  }

  private lateinit var _timings: Timings
  val timings
    get() = _timings

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    this.menu = menu
    return true
  }

  fun settings_menu_item_visibility(show: Boolean) {
    menu?.findItem(R.id.settings_action)?.isVisible = show
  }

  fun timetable_profiles_menu_item_visibility(show: Boolean) {
    menu?.findItem(R.id.timetable_profiles_action)?.isVisible = show
  }

  private var _is_timeAndWeekTextView_mutable = true
  val is_timeAndWeekTextView_mutable
    get() = _is_timeAndWeekTextView_mutable

  fun is_attached(fragment: Class<*>): Boolean {
    return (fragment == fragment_manager.primaryNavigationFragment!!.javaClass)
  }

  private fun clear_text_of_timeAndWeekTextView() {
    binding.timeAndWeekTextView.text = ""
  }

  fun set_text_of_timeAndWeekTextView(text: String) {
    if (!is_timeAndWeekTextView_mutable) return
    binding.timeAndWeekTextView.text = text
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    _is_timeAndWeekTextView_mutable = false
    return when (item.itemId) {
      R.id.settings_action -> {
        nav_controller.navigate(
          when (current_fragment) {
            is MainFragment -> {
              clear_text_of_timeAndWeekTextView()
              R.id.action_mainFragment_to_settingsFragment
            }
            is TimetableProfilesFragment ->
              R.id.action_timetableProfilesFragment_to_settingsFragment
            else -> return true
          }
        )
        true
      }
      R.id.backup_action -> {
        val zone_id = ZoneId.systemDefault()
        val iso_format = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        val time_str = Instant.now().atZone(zone_id).format(iso_format)
          .replace(':', '-')
        val app_name = applicationInfo.loadLabel(packageManager).toString()
        val backup_file_name = "${app_name}_backup_${time_str}.json"

        val dir =
          File(backup_dir.parentFile!!.name, backup_dir.name).absolutePath
        val snackbar =
          make_snackbar("Backup saved in $dir", Snackbar.LENGTH_LONG)

        lifecycleScope.launch {
          val gson = GsonBuilder().setPrettyPrinting().create()
          val db = DatabaseHelper.instance(this@MainActivity)
          val backup_text = gson.toJson(db.app_settingsDAO().get()!!)

          if (!backup_dir.exists()) backup_dir.mkdir()
          val file = File(backup_dir, backup_file_name)
          file.writeText(backup_text)

          snackbar.show()
        }
        true
      }
      R.id.restore_action -> {
        val list = backup_dir.list()
        if (list == null || list.isEmpty()) {
          make_snackbar(
            "No backups to restore from",
            Snackbar.LENGTH_LONG
          ).show()
          return true
        }
        val adapter =
          ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        val snackbar = make_snackbar("Settings restored", Snackbar.LENGTH_SHORT)

        val on_backup_file_selected = fun(
          _: DialogInterface,
          selected_index: Int,
        ) {
          val file_name = list[selected_index]
          val file = File(backup_dir, file_name)
          val restored_text = file.readText()

          lifecycleScope.launch {
            val db = DatabaseHelper.instance(this@MainActivity)
            val app_settings =
              Gson().fromJson(restored_text, AppSettings::class.java)
            db.app_settingsDAO().update(app_settings)

            LocalBroadcastManager
              .getInstance(this@MainActivity)
              .sendBroadcast(Intent(BROADCAST_ACTION_APP_SETTINGS_UPDATED))
            snackbar.show()
          }
        }

        AlertDialog
          .Builder(this, R.style.Theme_Timetable_AlertDialog)
          .setTitle("Which backup to restore from")
          .setAdapter(adapter, on_backup_file_selected)
          .create()
          .show()
        true
      }
      R.id.restore_from_file_action -> {
        backup_file_picker_launcher.launch("application/json")
        true
      }
      R.id.import_timetable_profile_action -> {
        timetable_profile_picker_launcher.launch("application/json")
        true
      }
      R.id.timetable_profiles_action -> {
        nav_controller.navigate(
          when (current_fragment) {
            is MainFragment -> {
              clear_text_of_timeAndWeekTextView()
              R.id.action_mainFragment_to_timetableProfilesFragment
            }
            is SettingsFragment ->
              R.id.action_settingsFragment_to_timetableProfilesFragment
            else -> return true
          }
        )
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    _is_timeAndWeekTextView_mutable = true
    return nav_controller.navigateUp(app_bar_configuration)
      || super.onSupportNavigateUp()
  }

  override fun onKeyDown(key_code: Int, event: KeyEvent?): Boolean {
    if (key_code == KeyEvent.KEYCODE_BACK) {
      if (onSupportNavigateUp()) return true
    }
    return super.onKeyDown(key_code, event)
  }

  fun make_snackbar(text: String, duration: Int): Snackbar {
    return ThemedSnackbar.make(
      this,
      binding.root,
      text,
      duration
    )
  }
}
