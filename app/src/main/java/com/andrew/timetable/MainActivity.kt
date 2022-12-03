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
  private val timings = utils.timings
  private val time_periods = utils.time_periods

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
      setTextColor(getColor(green))
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
        val next_week_if_today_is_sunday =
          if (current_day_of_week == Calendar.SUNDAY) 1 else 0
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

        // Color Timetable
        for ((week_day_index, week_day_TextView) in timetable.withIndex()) {
          // Start index from 1 -> "+1"
          // Start week from Monday -> "-1"
          val is_current_day_of_week =
            week_day_index + 1 == current_day_of_week - 1
          val week_day_color = if (is_current_day_of_week) yellow else green
          var color = week_day_color
          for ((pair_index, pair_TextView) in week_day_TextView.withIndex()) {
            if (pair_index == 0) {
              pair_TextView.setTextColor(getColor(color))
              continue
            }
            val is_study_time =
              utils.study_time(current_time, (pair_index - 1) * 2) is Pair<*, *>
            color = when {
              is_current_day_of_week && is_study_time -> red
              else -> week_day_color
            }
            pair_TextView.setTextColor(getColor(color))
          }
        }

        val current_recess = utils.get_recess(current_time)
        val is_recess_time = current_recess != null

        // Color Time
        for (i in time_periods.indices) {
          val color = when {
            is_recess_time && i == current_recess!!.time_period_index -> yellow
            utils.study_time(current_time, i) is Pair<*, *> -> red
            else -> green
          }
          timetable_for_time_periods[i].setTextColor(getColor(color))
        }

        val current_pair = utils.study_time(current_time)
        var index = -1
        var other_time = true
        when {
          current_pair is Pair<*, *> -> {
            when (current_pair.second) {
              1 -> index = (current_pair.first as Int - 1) * 4
              2 -> index = (current_pair.first as Int - 1) * 4 + 2
            }
          }
          current_recess != null -> index = current_recess.timing_index
        }
        if (index != -1) other_time = false

        val lessons_time_left = when {
          other_time || index % 2 == 1 -> "   --:--  "
          else -> timings[index + 1].minus(current_time).short_format()
        }

        val time_until_next_lesson = when {
          other_time || index > (timings.size - 3) -> {
            when {
              other_time && current_time < timings[0] -> timings[0]
                .minus(current_time).short_format()
              else -> "     --:--"
            }
          }
          else -> timings[index - index % 2 + 2]
            .minus(current_time).short_format()
        }

        val pairs_time_left = when {
          other_time || index % 4 == 3 -> "      --:--  "
          else -> timings[index - index % 4 + 3]
            .minus(current_time).short_format()
        }

        val time_until_next_pair = when {
          other_time || index > (timings.size - 5) -> {
            when {
              other_time && current_time < timings[0] -> timings[0]
                .minus(current_time).short_format()
              else -> "     --:--"
            }
          }
          else -> timings[index - index % 4 + 4]
            .minus(current_time).short_format()
        }

        binding.lessonTextView.text =
          "Lesson's time left: $lessons_time_left |" +
                  " Time until next lesson: $time_until_next_lesson"

        binding.pairTextView.text =
          "Pair's time left:    $pairs_time_left |" +
                  " Time until next pair:      $time_until_next_pair"

        loop_handler.postDelayed(this, 10)
      }
    })
  }
}
