package com.andrew.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrew.timetable.R.color.*
import com.andrew.timetable.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
  private val TEXT_SIZE = 16F // 18F
  private val utils = Utils()
  private val time_periods = utils.time_periods

  // Color aliases
  private val default_color = green
  private val current_day_color = yellow
  private val recess_color = current_day_color
  private val study_color = red

  private lateinit var binding: ActivityMainBinding

  private fun create_TextView(
    text: String,
    parent_layout: ViewGroup
  ): TextView {
    val text_view = TextView(this)
    with(text_view) {
      val width = RelativeLayout.LayoutParams.WRAP_CONTENT
      val height = RelativeLayout.LayoutParams.WRAP_CONTENT
      layoutParams = RelativeLayout.LayoutParams(width, height)
      setTextColor(getColor(default_color))
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
    week_day: String
  ) {
    val subjects: JSONObject = timetable_config[week_day] as JSONObject
    text_view_list += create_TextView(week_day, binding.subjectsLayout)
    for (lessons_order in subjects.keys()) {
      var subject = ""
      with(subjects[lessons_order]) {
        if (this is String) subject = this
//        when (this) {
//          is String -> subject = this
//          is Array<*> -> {
//
//          }
//        }
      }
      text_view_list += create_TextView(
        "$lessons_order. $subject", binding.subjectsLayout
      )
    }
  }

  private fun repopulate_SubjectLayout(
    timetable_configs: TimetableConfigs,
    timeTable: MutableList<MutableList<TextView>>,
    config: TimetableConfigs.Config = TimetableConfigs.Config.CURRENT
  ) {
    binding.subjectsLayout.removeAllViews()
    timeTable.forEach { it.clear() }
    for (week_day in
    timetable_configs.get_config(config).keys().withIndex()) {
      create_week_day_subject_table(
        timeTable[week_day.index],
        timetable_configs.get_current_config(),
        week_day.value
      )
    }
  }

  @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.timeAndWeekTextView.textSize = TEXT_SIZE
    // Fully transparent navigation & status bars
    window.setFlags(
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )

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
    val config_names = arrayOf("IMK4_5s.json", "IMK4_5s_custom.json")

    val timetable_configs = TimetableConfigs(assets, config_names, 1)
    val timetable = MutableList<MutableList<TextView>>(6) { mutableListOf() }
    repopulate_SubjectLayout(timetable_configs, timetable)
    binding.subjectsLayout.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
//          val x = event.x.toInt()
//          val y = event.y.toInt()
          repopulate_SubjectLayout(
            timetable_configs, timetable, TimetableConfigs.Config.NEXT
          )
        }
      }
      true
    }

    val timetable_for_time_periods = mutableListOf<TextView>()
    for (line in time_periods) {
      timetable_for_time_periods += create_TextView(line, binding.timeLayout)
    }

    val calendar = Calendar.getInstance()
    val loop_handler = Handler(Looper.getMainLooper())
    loop_handler.post(object : Runnable {
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
        val week = 1 + weeks_offset + next_week_if_today_is_sunday
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
        binding.timeAndWeekTextView.text =
          "week $current_week $formatted_time $week_parity"

        // Handle changes in timetable (numerator/denominator, visibility)
        val timetable_config = timetable_configs.get_current_config()
        for ((week_day_index, week_day) in
        timetable_config.keys().withIndex()) {
          val subjects = timetable_config[week_day] as JSONObject
          for (lessons_order in subjects.keys()) {
            // tmp is either a String or JSONArray with size 2
            // Example:
            // either "subject name"
            // or ["numerator subject", "denominator subject"]
            val tmp = subjects[lessons_order]
            if (tmp !is JSONArray) continue
            val subject_name = tmp[if (dnm) 1 else 0]
            val subject_TextView =
              timetable[week_day_index][lessons_order.toInt()]
            when (subject_name) {
              "" -> subject_TextView.visibility = View.GONE
              else -> {
                subject_TextView.text = "$lessons_order. $subject_name"
                // Check the other string in array
                if (tmp[if (!dnm) 1 else 0] as String === "")
                  subject_TextView.visibility = View.VISIBLE
              }
            }
          }
        }

        val current_pair = utils.get_pair(current_time)
        val current_recess = utils.get_recess(current_time)
        val is_recess_time = current_recess != null
        val is_study_time = current_pair != null

        // --------------------------|Color timetable|--------------------------
        // Color everything in default color
        for (week_day_TextViews in timetable) {
          for (pair_TextView in week_day_TextViews) {
            pair_TextView.setTextColor(getColor(default_color))
          }
        }

        if (!today_is_sunday) {
          // Start week from Monday => "-1"
          // Start index from 1 => "-1"
          val week_day_index = current_day_of_week - 2
          val week_day_TextViews = timetable[week_day_index]

          // Color current day of the week
          for (pair_TextView in week_day_TextViews) {
            pair_TextView.setTextColor(getColor(current_day_color))
          }

          // Color current pair
          if (is_study_time) {
            val pair_TextView = week_day_TextViews[current_pair!!.pair]
            pair_TextView.setTextColor(getColor(study_color))
          }
        }
        // --------------------------|Color timetable|--------------------------

        // ----------------------------|Color time|-----------------------------
        // Color all time periods
        for (i in time_periods.indices) {
          timetable_for_time_periods[i].setTextColor(getColor(default_color))
        }

        // Color current time periods
        if (is_study_time || is_recess_time) {
          val i = when {
            is_study_time -> current_pair!!.time_period_index
            else -> current_recess!!.time_period_index
          }
          val color = if (is_study_time) study_color else recess_color
          timetable_for_time_periods[i].setTextColor(getColor(color))
        }
        // ----------------------------|Color time|-----------------------------

        val lessons_time_left = utils.get_lesson_time_left(current_time)
        val time_until_next_lesson =
          utils.get_time_until_next_lesson(current_time)
        binding.lessonTextView.text =
          "Lesson's time left: $lessons_time_left |" +
                  " Time until next lesson: $time_until_next_lesson"

        val pairs_time_left = utils.get_pairs_time_left(current_time)
        val time_until_next_pair = utils.get_time_until_next_pair(current_time)
        binding.pairTextView.text =
          "Pair's time left:    $pairs_time_left |" +
                  " Time until next pair:      $time_until_next_pair"

        loop_handler.postDelayed(this, 10)
      }
    })
  }
}
