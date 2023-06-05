package com.andrew.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.andrew.timetable.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var nav_controller: NavController
  private lateinit var app_bar_configuration: AppBarConfiguration
  private lateinit var fragment_manager: FragmentManager
  private val current_fragment
    get() = fragment_manager.primaryNavigationFragment

  @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    return true
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
        if (current_fragment is SettingsFragment) return true
        clear_text_of_timeAndWeekTextView()
        nav_controller.navigate(R.id.settingsFragment)
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
}
