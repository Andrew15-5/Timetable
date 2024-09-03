package com.andrew.timetable

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andrew.timetable.MainActivity.Companion.BROADCAST_ACTION_APP_SETTINGS_UPDATED
import com.andrew.timetable.MainActivity.Companion.BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED
import com.andrew.timetable.databinding.FragmentMainBinding
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Month
import java.util.Calendar

class MainFragment : Fragment() {
  private val TEXT_SIZE = 16F // 18F
  private val utils = Utils()
  private val time_periods = utils.time_periods

  internal val timetable =
    MutableList<MutableList<TextView>>(6) { mutableListOf() }

  private var default_color: Int = 0
  private var current_day_color: Int = 0
  private var study_color: Int = 0
  private var recess_color: Int = 0
  private lateinit var binding: FragmentMainBinding
  private lateinit var app_settingsDAO: AppSettingsDAO
  private lateinit var timetable_profileDAO: TimetableProfileDAO

  internal lateinit var timetable_configs: TimetableConfigs

  private fun set_text_of_timeAndWeekTextView(text: String) = activity?.run {
    if ((this as MainActivity).is_attached(this@MainFragment.javaClass))
      set_text_of_timeAndWeekTextView(text)
  }

  private var timings: Timings? = null

  val broadcast_receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == BROADCAST_ACTION_APP_SETTINGS_UPDATED) {
        update_timings_visibility()
      }
      if (intent?.action == BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED) {
        update_timetable_configs()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    val intent_filter = IntentFilter()
    intent_filter.addAction(BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED)
    intent_filter.addAction(BROADCAST_ACTION_APP_SETTINGS_UPDATED)
    LocalBroadcastManager.getInstance(requireContext())
      .registerReceiver(broadcast_receiver, intent_filter)
  }

  override fun onPause() {
    LocalBroadcastManager.getInstance(requireContext())
      .unregisterReceiver(broadcast_receiver)
    super.onPause()
  }

  private fun create_TextView(
    text: String,
    parent_layout: ViewGroup,
  ): TextView {
    val text_view = TextView(activity)
    with(text_view) {
      val width = RelativeLayout.LayoutParams.WRAP_CONTENT
      val height = RelativeLayout.LayoutParams.WRAP_CONTENT
      layoutParams = RelativeLayout.LayoutParams(width, height)
      // Note: If color of the week day was changed from default, then
      // repopulation with default color is visible when switching between
      // timetable configs. Solution: Remember the color of each line.
      setTextColor(default_color)
      includeFontPadding = false
      textSize = TEXT_SIZE
      textAlignment = View.TEXT_ALIGNMENT_CENTER
      this.text = text
      parent_layout.addView(this)
    }
    return text_view
  }

  private fun create_week_day_subject_table(
    text_view_list: MutableList<TextView>,
    timetable_config: JSONObject,
    week_day: String,
  ) {
    val subjects: JSONObject = timetable_config[week_day] as JSONObject
    text_view_list += create_TextView(week_day, binding.subjectsLayout)
    for (subject_order in subjects.keys()) {
      val subject = subjects[subject_order]
      val subject_str = if (subject is String) subject else ""
      //      when (this) {
      //        is String -> subject_str = this
      //        is Array<*> -> {
      //
      //        }
      //      }
      text_view_list += create_TextView(
        "$subject_order. $subject_str", binding.subjectsLayout
      )
    }
  }

  internal fun repopulate_SubjectLayout(
    timetable_configs: TimetableConfigs,
    timeTable: MutableList<MutableList<TextView>>,
    config: TimetableConfigs.Config = TimetableConfigs.Config.CURRENT,
  ) {
    if (timetable_configs.isEmpty()) return
    binding.subjectsLayout.removeAllViews()
    timeTable.forEach { it.clear() }
    timetable_configs.get_config(config)?.keys()?.withIndex()
      ?.forEach { week_day ->
        create_week_day_subject_table(
          timeTable[week_day.index],
          timetable_configs.get_current_config()!!,
          week_day.value
        )
      }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  fun update_timings_visibility() {
    val visibility = mapOf(true to View.VISIBLE, false to View.GONE)
    lifecycleScope.launch {
      timings = app_settingsDAO.get()!!.timings
      timings?.apply {
        binding.timeSinceLessonStarted.visibility =
          visibility[this.time_since_lesson_started]!!
        binding.timeSinceHalfStarted.visibility =
          visibility[this.time_since_half_started]!!
        binding.lessonsTimeLeft.visibility =
          visibility[this.lessons_time_left]!!
        binding.halfsTimeLeft.visibility =
          visibility[this.halfs_time_left]!!
        binding.timeUntilNextLesson.visibility =
          visibility[this.time_until_next_lesson]!!
        binding.timeUntilNextHalf.visibility =
          visibility[this.time_until_next_half]!!
      }
    }
  }

  fun update_timetable_configs() {
    lifecycleScope.launch {
      val profiles = timetable_profileDAO.get_all()

      if (profiles.isEmpty()) {
        binding.timetableProfileNameTextView.visibility = View.GONE
        binding.noTimetableProfiles.visibility = View.VISIBLE
        return@launch
      }
      binding.noTimetableProfiles.visibility = View.GONE
      binding.timetableProfileNameTextView.visibility = View.VISIBLE

      timetable_configs.configs.clear()
      timetable_configs.configs.addAll(profiles.map { it.timetable })
      timetable_configs.config_names.clear()
      timetable_configs.config_names.addAll(profiles.map { it.name })
      repopulate_SubjectLayout(timetable_configs, timetable)
    }
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val activity = activity as MainActivity

    timetable_configs = TimetableConfigs(activity.assets, arrayOf())

    lifecycleScope.launch {
      val db = DatabaseHelper.instance(activity)
      app_settingsDAO = db.app_settingsDAO()
      timetable_profileDAO = db.timetable_profileDAO()
      update_timings_visibility()
      update_timetable_configs()
    }

    default_color = activity.getColor(R.color.default_color)
    current_day_color = activity.getColor(R.color.current_day_color)
    study_color = activity.getColor(R.color.study_color)
    recess_color = activity.getColor(R.color.recess_color)

    // Better to quickly show needed timings than hide unneeded <- more visible
    binding.timeSinceLessonStarted.visibility = View.GONE
    binding.timeSinceHalfStarted.visibility = View.GONE
    binding.lessonsTimeLeft.visibility = View.GONE
    binding.halfsTimeLeft.visibility = View.GONE
    binding.timeUntilNextLesson.visibility = View.GONE
    binding.timeUntilNextHalf.visibility = View.GONE

    binding.importTimetableButton.setOnClickListener {
      activity.import_timetable_profile()
    }

    // Fully transparent navigation & status bars
    // window.setFlags(
    //   WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
    //   WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    // )

    // JSON nth subject values:
    // 1. "Subject" - same "Subject" every week
    // 2. ["Numerator subject", "Denominator subject"] -
    //    "Numerator subject" on numerator week,
    //    "Denominator subject" on denominator week
    // 3.1. ["", "Denominator subject"] -
    //      no nth subject on numerator week,
    //      "Denominator subject" on denominator week
    // 3.2. ["Numerator subject", ""] -
    //      "Numerator subject" on numerator week,
    //      no nth subject on denominator week
    // Note: instead of empty "" you can use "—" or anything else. This is
    // useful for intermediate (non-last) pairs which will not disappear with ""
    // unlike the last pair.

    val timetable_for_time_periods = mutableListOf<TextView>()
    for (line in time_periods) {
      timetable_for_time_periods += create_TextView(line, binding.timeLayout)
    }

    val calendar = Calendar.getInstance()
    val loop_handler = Handler(Looper.getMainLooper())
    loop_handler.post(object : Runnable {
      @SuppressLint("SetTextI18n")
      override fun run() {

        utils.set_calendar_date_for_today(calendar)

        // Note: Sunday = 1 .. Saturday = 7
        val current_day_of_week = calendar.get(Calendar.DAY_OF_WEEK)
        val current_year = calendar.get(Calendar.YEAR)
        val current_week_of_year = utils.get_week_of_year(calendar)

        val start_date_of_current_semester =
          utils.get_start_date_of_current_semester(calendar)
        val first_week_of_semester =
          utils.get_week_of_year(start_date_of_current_semester)

        val weeks_offset = current_week_of_year - first_week_of_semester
        // Note: timetable and week parity is changing every Sunday
        val today_is_sunday = current_day_of_week == Calendar.SUNDAY
        val next_week_if_today_is_sunday = if (today_is_sunday) 1 else 0
        var week = 1 + weeks_offset + next_week_if_today_is_sunday

        // If 1st day of September is Sunday, then don't count it as the 1st week (e.g., 2024-09-01).
        if (start_date_of_current_semester.month == Month.SEPTEMBER
          && start_date_of_current_semester.dayOfWeek == DayOfWeek.SUNDAY
        ) {
          week -= 1
        }

        var dnm = week % 2 == 0 // Abbreviation for denominator

        // Starting from 11th week (only in 3rd semester) week parity is swapped
        if (current_year == 2021 && week >= 11) {
          dnm = !dnm
        }

        val current_time = Time.now()
        val formatted_time = current_time.full_format()
        val week_parity = if (dnm) "denominator" else "numerator"
        val last_week = 17
        val current_week = if (week in 1..last_week) week else "18+"

        set_text_of_timeAndWeekTextView(
          "week $current_week $formatted_time $week_parity"
        )

        // Handle changes in timetable (numerator/denominator, visibility)
        val timetable_config_name = timetable_configs.get_current_config_name()
        timetable_config_name?.apply {
          binding.timetableProfileNameTextView.text = this
        }
        val timetable_config = timetable_configs.get_current_config()
        timetable_config?.keys()?.withIndex()
          ?.forEach { (week_day_index, week_day) ->
            val subjects = timetable_config[week_day] as JSONObject
            for (subject_order in subjects.keys()) {
              // tmp is either a String or JSONArray with size 2
              // Example:
              // either "subject name"
              // or ["numerator subject", "denominator subject"]
              val tmp = subjects[subject_order]
              if (tmp !is JSONArray) continue // Skip "String" subjects
              val subject_name = tmp[if (dnm) 1 else 0]
              val subject_TextView =
                timetable[week_day_index][subject_order.toInt()]
              when (subject_name) {
                "" -> subject_TextView.visibility = View.GONE
                else -> {
                  subject_TextView.text = "$subject_order. $subject_name"
                  // Check the other string in array
                  if (tmp[if (!dnm) 1 else 0] as String === "")
                    subject_TextView.visibility = View.VISIBLE
                }
              }
            }
          }

        val current_lesson = utils.get_lesson(current_time)
        val current_recess = utils.get_recess(current_time)
        val is_recess_time = current_recess != null
        val is_study_time = current_lesson != null

        // --------------------------|Color timetable|--------------------------
        // Color everything in default color
        for (week_day_TextViews in timetable) {
          for (lesson_TextView in week_day_TextViews) {
            lesson_TextView.setTextColor(default_color)
          }
        }

        if (!today_is_sunday) {
          // Start week from Monday => "-1"
          // Start index from 1 => "-1"
          val week_day_index = current_day_of_week - 2
          val week_day_TextViews = timetable[week_day_index]

          // Color current day of the week
          for (lesson_TextView in week_day_TextViews) {
            lesson_TextView.setTextColor(current_day_color)
          }

          // Color current lesson
          val lesson_number = current_lesson?.lesson
          if (is_study_time && week_day_TextViews.size > lesson_number!!) {
            val lesson_TextView = week_day_TextViews[lesson_number]
            lesson_TextView.setTextColor(study_color)
          }
        }
        // --------------------------|Color timetable|--------------------------

        // ----------------------------|Color time|-----------------------------
        // Color all time periods
        for (i in time_periods.indices) {
          timetable_for_time_periods[i].setTextColor(default_color)
        }

        // Color current time periods
        if (is_study_time || is_recess_time) {
          val i = when {
            is_study_time -> current_lesson!!.time_period_index
            else -> current_recess!!.time_period_index
          }
          val color =
            if (is_study_time) study_color else recess_color
          timetable_for_time_periods[i].setTextColor(color)
        }
        // ----------------------------|Color time|-----------------------------

        val time_since_lesson_started =
          utils.get_time_since_lesson_started(current_time)
        val time_since_half_started =
          utils.get_time_since_half_started(current_time)

        val lessons_time_left = utils.get_lessons_time_left(current_time)
        val halfs_time_left = utils.get_half_time_left(current_time)

        val time_until_next_lesson =
          utils.get_time_until_next_lesson(current_time)
        val time_until_next_half =
          utils.get_time_until_next_half(current_time)

        binding.timeSinceLessonStartedTextView.text = time_since_lesson_started
        binding.timeSinceHalfStartedTextView.text = time_since_half_started
        binding.lessonsTimeLeftTextView.text = lessons_time_left
        binding.halfsTimeLeftTextView.text = halfs_time_left
        binding.timeUntilNextLessonTextView.text = time_until_next_lesson
        binding.timeUntilNextHalfTextView.text = time_until_next_half

        loop_handler.postDelayed(this, 10)
      }
    })
  }
}