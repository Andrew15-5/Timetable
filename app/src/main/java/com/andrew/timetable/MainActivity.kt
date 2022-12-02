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
import org.threeten.bp.*
import org.threeten.bp.temporal.TemporalAdjusters
import org.threeten.bp.temporal.WeekFields
import java.util.*


class MainActivity : AppCompatActivity() {
  private val TEXT_SIZE = 16F // 18F

  private fun create_TextView(str: String, parent_layout: ViewGroup): TextView {
    val text_view = TextView(this)
    with(text_view) {
      val width = RelativeLayout.LayoutParams.WRAP_CONTENT
      val height = RelativeLayout.LayoutParams.WRAP_CONTENT
      layoutParams = RelativeLayout.LayoutParams(width, height)
      setTextColor(getColor(green))
      includeFontPadding = false
      textSize = TEXT_SIZE
      textAlignment = View.TEXT_ALIGNMENT_CENTER
      text = str
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
    text_view_list.add(create_TextView(week_day, binding.subjectsLayout))
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
      text_view_list.add(create_TextView("$lessons_order. $subject", binding.subjectsLayout))
    }
  }

  private fun repopulate_SubjectLayout(
    timetable_configs: TimetableConfigs,
    timeTable: MutableList<MutableList<TextView>>,
    config_index: TimetableConfigs.Config = TimetableConfigs.Config.CURRENT
  ) {
    binding.subjectsLayout.removeAllViews()
    timeTable.forEach { it.clear() }
    for (week_day in timetable_configs.config(config_index).keys().withIndex()) {
      create_week_day_subject_table(
        timeTable[week_day.index],
        timetable_configs.current_config(),
        week_day.value
      )
    }
  }


  fun get_start_date_of_current_semester(
    year: Int, month: Int
  ): LocalDate {
    val first_monday_of_february: Int = LocalDate.of(year, Month.FEBRUARY, 1)
      .with(TemporalAdjusters.dayOfWeekInMonth(1, DayOfWeek.MONDAY))
      .dayOfMonth
    return if (month >= Month.SEPTEMBER.value) { // 1st semester in the academic year
      LocalDate.of(year, Month.SEPTEMBER, 1)
    } else { // Get the beginning of the 2nd week of February (2nd semester in the academic year)
      if (first_monday_of_february == 1) {
        LocalDate.of(year, Month.FEBRUARY, 8)
      } else {
        LocalDate.of(year, Month.FEBRUARY, first_monday_of_february)
      }
    }
  }

  fun get_start_date_of_current_semester(calendar: Calendar): LocalDate {
    return get_start_date_of_current_semester(
      calendar[Calendar.YEAR],
      calendar[Calendar.MONTH] + 1
    )
  }

  /**
   * This is the only one method of getting week of year that works correctly (made test for it).
   * Range of return value is from 1 to 53 (both included).
   */
  fun get_week_of_year(year: Int, month: Int, day: Int): Int {
    val week_of_year = WeekFields.of(DayOfWeek.MONDAY, 1).weekOfYear()
    val date = LocalDate.of(year, month, day)
    return date.get(week_of_year)
  }


  fun get_week_of_year(date: LocalDate): Int {
    return get_week_of_year(date.year, date.monthValue, date.dayOfMonth)
  }

  fun get_week_of_year(calendar: Calendar): Int {
    return get_week_of_year(
      calendar[Calendar.YEAR],
      calendar[Calendar.MONTH] + 1,
      calendar[Calendar.DAY_OF_MONTH]
    )
  }


  private lateinit var binding: ActivityMainBinding

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
    // 1) "Subject" -
    //   same "Subject" every week
    // 2) ["Numerator subject", "Denominator subject"] -
    //   "Numerator subject" on numerator week, "Denominator subject" on denominator week
    // 3.1) ["", "Denominator subject"] -
    //   no nth subject on numerator week, "Denominator subject" on denominator subject
    // 3.2) ["Numerator subject", ""] -
    //   "Numerator subject" on numerator subject, no nth subject on denominator week
    val config_names = arrayOf("IMK4_5s.json", "IMK4_5s_custom.json")

    val timetable_configs = TimetableConfigs(assets, config_names, 1)
    val timetable = MutableList<MutableList<TextView>>(6) { mutableListOf() }
    repopulate_SubjectLayout(timetable_configs, timetable)
    binding.subjectsLayout.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
//          val x = event.x.toInt()
//          val y = event.y.toInt()
          repopulate_SubjectLayout(timetable_configs, timetable, TimetableConfigs.Config.NEXT)
        }
      }
      true
    }

    val timings = mutableListOf<Time>()
    arrayOf(
      "8:30", "9:15", "9:20", "10:05",
      "10:20", "11:05", "11:10", "11:55",
      "12:10", "12:55", "13:00", "13:45",
      "14:15", "15:00", "15:05", "15:50",
      "16:05", "15:50", "16:55", "17:40",
      "17:50", "18:35", "18:40", "19:25"
    ).forEach { time -> timings += Time.from_hhmm(time) }

    val time_period = arrayOf(
      "8:30 - 9:15 | 9:20 -10:05",
      "10:05-10:20",
      "10:20-11:05 | 11:10-11:55",
      "11:55-12:10",
      "12:10-12:55 | 13:00-13:45",
      "13:45-14:15",
      "14:15-15:00 | 15:05-15:50",
      "15:50-16:05",
      "16:05-16:50 | 16:55-17:40",
      "17:40-17:50",
      "17:50-18:35 | 18:40-19:25"
    )

    val timeTimeTable = mutableListOf<TextView>()
    for (line in time_period) {
      timeTimeTable.add(create_TextView(line, binding.timeLayout))
    }

    fun study_time(x: Time, y: Int = -1): Any {
      var i = 0
      if (y == -1 || y == 0) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(1, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(1, 2)
      }
      if (y == -1 || y == 2) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(2, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(2, 2)
      }
      if (y == -1 || y == 4) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(3, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(3, 2)
      }
      if (y == -1 || y == 6) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(4, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(4, 2)
      }
      if (y == -1 || y == 8) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(5, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(5, 2)
      }
      if (y == -1 || y == 10) {
        if (x.from_until(timings[i++], timings[i++])) return Pair(6, 1)
        else if (x.from_until(timings[i++], timings[i++])) return Pair(6, 2)
      }
      return false
    }

    fun break_time(x: Time): Any {
      var i = 1
      return when {
        x.from_until(timings[i++], timings[i++]) -> 1
        x.from_until(timings[i++], timings[i++]) -> 12
        x.from_until(timings[i++], timings[i++]) -> 2
        x.from_until(timings[i++], timings[i++]) -> 23
        x.from_until(timings[i++], timings[i++]) -> 3
        x.from_until(timings[i++], timings[i++]) -> 34
        x.from_until(timings[i++], timings[i++]) -> 4
        x.from_until(timings[i++], timings[i++]) -> 45
        x.from_until(timings[i++], timings[i++]) -> 5
        x.from_until(timings[i++], timings[i++]) -> 56
        x.from_until(timings[i++], timings[i++]) -> 6
        else -> false
      }
    }

    fun correct_break(x: Time, y: Double): Boolean {
      when (val ret = break_time(x)) {
        false -> return false
        is Int -> return when {
          ret < 10 -> (ret - 0.5 == y)
          else -> ((ret / 10).toDouble() == y)
        }
      }
      return false
    }

    val calendar = Calendar.getInstance()
    val loop_handler = Handler(Looper.getMainLooper())
    loop_handler.post(object : Runnable {
      override fun run() {

        fun set_calendar_date_for_today(calendar: Calendar) {
          val current_date = LocalDate.now()
          // Note: month count in Calendar starts with 0 for JANUARY
          calendar.set(current_date.year, current_date.monthValue - 1, current_date.dayOfMonth)
        }

        set_calendar_date_for_today(calendar)
        val current_day_of_week = calendar.get(Calendar.DAY_OF_WEEK) // For coloring
        val current_year = calendar.get(Calendar.YEAR)
        val current_week_of_year = get_week_of_year(calendar)

        val start_date_of_current_semester = get_start_date_of_current_semester(calendar)
        val first_week_of_semester = get_week_of_year(start_date_of_current_semester)

        val weeks_offset = current_week_of_year - first_week_of_semester
        // Note: timetable and week parity is changing every Sunday
        val week = 1 + weeks_offset + if (current_day_of_week == Calendar.SUNDAY) 1 else 0
        var dnm = week % 2 == 0 // Abbreviation for denominator

        // Starting from 11th week (only in 3rd semester) week parity is swapped
        if (current_year == 2021 && week >= 11) {
          dnm = !dnm
        }

        val current_time = Time.now().full_format()
        val week_parity = if (dnm) "denominator" else "numerator"
        val last_week = 17
        val current_week = if (week in 1..last_week) week else "18+"
        binding.timeAndWeekTextView.text = "week $current_week $current_time $week_parity"

        // Handle changes in timetable (numerator/denominator, visibility)
        for (indexed_week_day in timetable_configs.current_config().keys().withIndex()) {
          val subjects = timetable_configs.current_config()[indexed_week_day.value] as JSONObject
          for (lessons_order in subjects.keys()) {
            // tmp is either a String or JSONArray with size 2
            // Example: either "subject name" or ["numerator subject", "denominator subject"]
            val tmp = subjects[lessons_order]
            if (tmp !is JSONArray) continue
            val subject = tmp[if (dnm) 1 else 0]
            val subject_text_view = timetable[indexed_week_day.index][lessons_order.toInt()]
            when (subject) {
              "" -> subject_text_view.visibility = View.GONE
              else -> {
                subject_text_view.text = "$lessons_order. $subject"
                // Check the other string in array
                if (tmp[if (!dnm) 1 else 0] as String === "")
                  subject_text_view.visibility = View.VISIBLE
              }
            }
          }
        }

        // Color Timetable
        val t = Time.now()
        for ((week_day, Day) in timetable.withIndex()) {
          val default_color = (if (week_day + 2 == current_day_of_week) yellow else green)
          var color = default_color
          for ((i, pair) in Day.withIndex()) {
            if (i == 0) {
              pair.setTextColor(getColor(color))
            } else {
              color = when {
                week_day + 2 == current_day_of_week &&
                        study_time(t, (i - 1) * 2) is Pair<*, *> -> red
                else -> default_color
              }
              pair.setTextColor(getColor(color))
            }
          }
        }

        // Color Time
        for (i in time_period.indices) {
          var color = if (correct_break(t, (i + 1) / 2.0)) yellow else green
          if (study_time(t, i) is Pair<*, *>) color = red
          timeTimeTable[i].setTextColor(getColor(color))
        }

        val current_break = break_time(t)
        val current_pair = study_time(t)
        var index = -1
        var other_time = true
        when {
          current_pair is Pair<*, *> -> {
            when (current_pair.second) {
              1 -> index = (current_pair.first as Int - 1) * 4
              2 -> index = (current_pair.first as Int - 1) * 4 + 2
            }
          }
          current_break is Int -> {
            when {
              current_break > 10 -> index = (current_break / 10 - 1) * 4 + 3
              current_break < 10 -> index = (current_break - 1) * 4 + 1
            }
          }
        }
        if (index != -1) other_time = false

        val lessons_time_left = when {
          other_time || index % 2 == 1 -> "   --:--  "
          else -> timings[index + 1].minus(t).short_format()
        }
        val time_until_next_lesson = when {
          other_time || index > (timings.size - 3) -> {
            when {
              other_time && t < timings[0] -> timings[0].minus(t).short_format()
              else -> "     --:--"
            }
          }
          else -> timings[index - index % 2 + 2].minus(t).short_format()
        }

        val pairs_time_left = when {
          other_time || index % 4 == 3 -> "      --:--  "
          else -> timings[index - index % 4 + 3].minus(t).short_format()
        }
        val time_until_next_pair = when {
          other_time || index > (timings.size - 5) -> {
            when {
              other_time && t < timings[0] -> timings[0].minus(t).short_format()
              else -> "     --:--"
            }
          }
          else -> timings[index - index % 4 + 4].minus(t).short_format()
        }
        binding.lessonTextView.text =
          "Lesson's time left: $lessons_time_left | Time until next lesson: $time_until_next_lesson"
        binding.pairTextView.text =
          "Pair's time left:    $pairs_time_left | Time until next pair:      $time_until_next_pair"

        loop_handler.postDelayed(this, 10)
      }
    })
  }
}
