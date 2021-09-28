package com.andrew.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrew.timetable.R.color.*
import com.andrew.timetable.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import java.util.*


class MainActivity : AppCompatActivity() {
  private val TEXT_SIZE = 16F // 18F

  private fun getTime(str: Any = "now", returnType: String = "string"): Any {
    val h: Int
    val m: Int
    val s: Int
    if (str == "now") {
      with(LocalDateTime.now()) {
        h = this.hour
        m = this.minute
        s = this.second
        return when (returnType) {
          "string" -> return "$h:${if (m < 10) "0" else ""}$m:${if (s < 10) "0" else ""}$s"
          "int" -> h * 3600 + m * 60 + s
          else -> arrayOf(h, m, s)
        }
      }
    }
    with(str as Int) {
      h = this / 3600 % 24
      m = this % 3600 / 60
      s = this % 60
    }
    return when (returnType) {
      "string" -> "${if (h != 0) "$h:" else ""}${if (m < 10) "0" else ""}$m:${if (s < 10) "0" else ""}$s"
      "int" -> str % 86400
      else -> arrayOf(h, m, s)
    }
  }

  private fun createTextView(str: String, toSubjectLayout: Boolean = true): TextView {
    val textView = TextView(this)
    with(textView) {
      val width = RelativeLayout.LayoutParams.WRAP_CONTENT
      val height = RelativeLayout.LayoutParams.WRAP_CONTENT
      layoutParams = RelativeLayout.LayoutParams(width, height)
      setTextColor(getColor(green))
      includeFontPadding = false
      textSize = TEXT_SIZE
      textAlignment = View.TEXT_ALIGNMENT_CENTER
      text = str
      when {
        toSubjectLayout -> binding.SubjectsLayout.addView(this)
        else -> binding.TimeLayout.addView(this)
      }
    }
    return textView
  }

  private fun createWeekDaySubjectTable(
    textViewList: MutableList<TextView>,
    timetable_config: JSONObject,
    week_day: String
  ) {
    val subjects: JSONObject = timetable_config[week_day] as JSONObject
    textViewList.add(createTextView(week_day))
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
      textViewList.add(createTextView("$lessons_order. $subject"))
    }
  }

  private fun repopulate_SubjectLayout(
    timetable_configs: TimetableConfigs,
    timeTable: MutableList<MutableList<TextView>>,
    config_index: TimetableConfigs.Config = TimetableConfigs.Config.CURRENT
  ) {
    binding.SubjectsLayout.removeAllViews()
    timeTable.forEach { it.clear() }
    for (week_day in timetable_configs.config(config_index).keys().withIndex()) {
      createWeekDaySubjectTable(
        timeTable[week_day.index],
        timetable_configs.current_config(),
        week_day.value
      )
    }
  }

  private lateinit var binding: ActivityMainBinding

  @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    AndroidThreeTen.init(this)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.TimeAndWeek.textSize = TEXT_SIZE
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
    val config_names = arrayOf("IMK4_3s.json", "IMK4_3s_custom.json")

    val timetable_configs = TimetableConfigs(assets, config_names, 1)
    val timeTable = MutableList<MutableList<TextView>>(6) { mutableListOf() }
    repopulate_SubjectLayout(timetable_configs, timeTable)
    binding.SubjectsLayout.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
//          val x = event.x.toInt()
//          val y = event.y.toInt()
          repopulate_SubjectLayout(timetable_configs, timeTable, TimetableConfigs.Config.NEXT)
        }
      }
      true
    }

    val timings = arrayOf(
      30600, 33300, 33600, 36300,
      37200, 39900, 40200, 42900,
      43800, 46500, 46800, 49500,
      51300, 54000, 54300, 57000,
      57900, 60600, 60900, 63600,
      64200, 66900, 67200, 69900
    )

    val timePeriod = arrayOf(
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
    for (line in timePeriod) {
      timeTimeTable.add(createTextView(line, false))
    }

    fun studyTime(x: Int, y: Int = -1): Any {
      if (y == -1 || y == 0) {
        if (x in timings[0] until timings[1]) return Pair(1, 1)
        else if (x in timings[2] until timings[3]) return Pair(1, 2)
      }
      if (y == -1 || y == 2) {
        if (x in timings[4] until timings[5]) return Pair(2, 1)
        else if (x in timings[6] until timings[7]) return Pair(2, 2)
      }
      if (y == -1 || y == 4) {
        if (x in timings[8] until timings[9]) return Pair(3, 1)
        else if (x in timings[10] until timings[11]) return Pair(3, 2)
      }
      if (y == -1 || y == 6) {
        if (x in timings[12] until timings[13]) return Pair(4, 1)
        else if (x in timings[14] until timings[15]) return Pair(4, 2)
      }
      if (y == -1 || y == 8) {
        if (x in timings[16] until timings[17]) return Pair(5, 1)
        else if (x in timings[18] until timings[19]) return Pair(5, 2)
      }
      if (y == -1 || y == 10) {
        if (x in timings[20] until timings[21]) return Pair(6, 1)
        else if (x in timings[22] until timings[23]) return Pair(6, 2)
      }
      return false
    }

    fun breakTime(x: Int): Any {
      return when (x) {
        in timings[1] until timings[2] -> 1
        in timings[3] until timings[4] -> 12
        in timings[5] until timings[6] -> 2
        in timings[7] until timings[8] -> 23
        in timings[9] until timings[10] -> 3
        in timings[11] until timings[12] -> 34
        in timings[13] until timings[14] -> 4
        in timings[15] until timings[16] -> 45
        in timings[17] until timings[18] -> 5
        in timings[19] until timings[20] -> 56
        in timings[21] until timings[22] -> 6
        else -> false
      }
    }

    fun correctBreak(x: Int, y: Double): Boolean {
      when (val ret = breakTime(x)) {
        false -> return false
        is Int -> return when {
          ret < 10 -> (ret - 0.5 == y)
          else -> ((ret / 10).toDouble() == y)
        }
      }
      return false
    }

    val calendar = Calendar.getInstance()
    // Some weeks of year are wrong if minimalDaysInFirstWeek is set to default
    calendar.minimalDaysInFirstWeek = 7
    val loopHandler = Handler(Looper.getMainLooper())
    loopHandler.post(object : Runnable {
      override fun run() {

        fun set_calendar_date_for_today(calendar: Calendar) {
          val current_date = LocalDate.now()
          // Note: month count in Calendar starts with 0 for JANUARY
          calendar.set(current_date.year, current_date.monthValue - 1, current_date.dayOfMonth)
        }

        fun set_calendar_date(calendar: Calendar, year: Int, month: Int, day: Int) {
          // Note: month count in Calendar starts with 0 for JANUARY
          calendar.set(year, month - 1, day)
        }

        set_calendar_date_for_today(calendar)
        val current_day_of_week = calendar.get(Calendar.DAY_OF_WEEK) // For coloring
        val current_year = calendar.get(Calendar.YEAR)
        val current_week_of_year = calendar.get(Calendar.WEEK_OF_YEAR)

        set_calendar_date(calendar, current_year, 9, 1)
        val first_week_of_academic_year = calendar.get(Calendar.WEEK_OF_YEAR)

        val weeks_offset = current_week_of_year - first_week_of_academic_year
        // Note: week starts with Sunday -> timetable and week parity will change on Sunday
        val week = weeks_offset + 1
        val dnm = week % 2 == 0 // abbreviation for denominator
        binding.TimeAndWeek.text =
          "week $week ${getTime()} ${if (dnm) "denominator" else "numerator"}"

        // Handle changes in timetable (numerator/denominator, visibility)
        for (indexed_week_day in timetable_configs.current_config().keys().withIndex()) {
          val subjects = timetable_configs.current_config()[indexed_week_day.value] as JSONObject
          for (lessons_order in subjects.keys()) {
            // tmp is either a String or JSONArray with size 2
            // Example: either "subject name" or ["numerator subject", "denominator subject"]
            val tmp = subjects[lessons_order]
            if (tmp !is JSONArray) continue
            val subject = tmp[if (dnm) 1 else 0]
            val subject_text_view = timeTable[indexed_week_day.index][lessons_order.toInt()]
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
        val t = getTime("now", "int") as Int
        for ((weekDay, Day) in timeTable.withIndex()) {
          val defaultColor = (if (weekDay + 2 == current_day_of_week) yellow else green)
          var color = defaultColor
          for ((i, pair) in Day.withIndex()) {
            if (i == 0) {
              pair.setTextColor(getColor(color))
            } else {
              color = when {
                weekDay + 2 == current_day_of_week && studyTime(t, (i - 1) * 2) is Pair<*, *> -> red
                else -> defaultColor
              }
              pair.setTextColor(getColor(color))
            }
          }
        }

        // Color Time
        for (i in timePeriod.indices) {
          var color = if (correctBreak(t, (i + 1) / 2.0)) yellow else green
          if (studyTime(t, i) is Pair<*, *>) color = red
          timeTimeTable[i].setTextColor(getColor(color))
        }

        val currentBreak = breakTime(t)
        val currentPair = studyTime(t)
        var index = -1
        var otherTime = true
        when {
          currentPair is Pair<*, *> -> {
            when (currentPair.second) {
              1 -> index = (currentPair.first as Int - 1) * 4
              2 -> index = (currentPair.first as Int - 1) * 4 + 2
            }
          }
          currentBreak is Int -> {
            when {
              currentBreak > 10 -> index = (currentBreak / 10 - 1) * 4 + 3
              currentBreak < 10 -> index = (currentBreak - 1) * 4 + 1
            }
          }
        }
        if (index != -1) otherTime = false


        var lessonsTimeLeft = ""
        when {
          otherTime || index % 2 == 1 -> lessonsTimeLeft = "   --:--  "
          index % 2 == 0 -> lessonsTimeLeft = getTime(timings[index + 1] - t) as String
        }

        var timeUntilNextLesson: String
        when {
          otherTime || index > (timings.size - 3) -> {
            timeUntilNextLesson = "     --:--"
            if (otherTime && t < 30600) {
              timeUntilNextLesson = getTime(timings[0] - t) as String
              if (timeUntilNextLesson.length < 7) timeUntilNextLesson = "   $timeUntilNextLesson"
            }
          }
          else -> {
            timeUntilNextLesson = getTime(timings[index - index % 2 + 2] - t) as String
            if (timeUntilNextLesson.length < 7) timeUntilNextLesson = "   $timeUntilNextLesson"
          }
        }

        var pairsTimeLeft: String
        when {
          otherTime || index % 4 == 3 -> pairsTimeLeft = "      --:--  "
          else -> {
            pairsTimeLeft = getTime(timings[index - index % 4 + 3] - t) as String
            if (pairsTimeLeft.length < 7) pairsTimeLeft = "   $pairsTimeLeft"
          }
        }
        var timeUntilNextPair: String
        when {
          otherTime || index > (timings.size - 5) -> {
            timeUntilNextPair = "     --:--"
            if (otherTime && t < 30600) {
              timeUntilNextPair = getTime(timings[0] - t) as String
              if (timeUntilNextPair.length < 7) timeUntilNextPair = "   $timeUntilNextPair"
            }
          }
          else -> {
            timeUntilNextPair = getTime(timings[index - index % 4 + 4] - t) as String
            if (timeUntilNextPair.length < 7) timeUntilNextPair = "   $timeUntilNextPair"
          }
        }
        binding.LessonTextView.text =
          "Lesson's time left: $lessonsTimeLeft | Time until next lesson: $timeUntilNextLesson"
        binding.PairTextView.text =
          "Pair's time left:    $pairsTimeLeft | Time until next pair:      $timeUntilNextPair"

        loopHandler.postDelayed(this, 10)
      }
    })
  }
}
