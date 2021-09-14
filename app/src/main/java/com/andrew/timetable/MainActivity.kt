package com.andrew.timetable

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrew.timetable.R.color.*
import com.andrew.timetable.databinding.ActivityMainBinding
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoField
import java.util.*


@Suppress("LocalVariableName", "PrivatePropertyName")
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
    subjectList: Array<String>,
    weekDay: String
  ) {
    textViewList.add(createTextView(weekDay))
    for (i in 1..subjectList.size) {
      textViewList.add(createTextView("$i. ${subjectList[i - 1]}"))
    }
  }

  private lateinit var binding: ActivityMainBinding

  @SuppressLint("SetTextI18n")
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

    val Monday = arrayOf(
      "", // - / Physics 1-415 (exercise)
      "Physics 1-414 (lab)",
      "PE 2"
    )
    // 4. Machine-dependent PL 5-222 (lab) II
    val Tuesday = arrayOf(
      "Physics 5-232 (lecture)",
      "", // Philosophy 5-232 (lecture) / Machine-dependent PL 5-232 (lecture)
      "Discrete Mathematics 7-404 (exercise)"
    )
    val Wednesday = arrayOf(
      "High-level programming 5-232 (lecture)",
      "Fundamentals of electr. 5-232 (lecture)", // electronics
      "PE 2",
      ""
    ) // Discrete Mathematics 3-203 (lab) I / II
    val Thursday = arrayOf(
      "English 1 (exercise)",
      "Discrete Mathematics 1-415 (lecture)",
      "Machine-dependent PL 5-222 (lab)"
    ) // I
    val Friday = arrayOf(
      "-",
      "-",
      "", // Machine-dependent PL 5-222 (lab) II / I
      "Philosophy 5-210 (exercise)",
      "", // High-level programming 5-219 (lab) I / II
      ""
    ) // High-level programming 5-219 (lab) I / II
    val Saturday = arrayOf(
      "", // High-level programming 5-219 (exercise) / Fundamentals of electronics 5-231 (exercise)
      ""
    ) // Fundamentals of electronics 5-231 (lab) I / II

    val timeTable = mutableListOf<MutableList<TextView>>()
    for (i in 0..5) timeTable.add(mutableListOf())

    createWeekDaySubjectTable(timeTable[0], Monday, "Monday")
    createWeekDaySubjectTable(timeTable[1], Tuesday, "Tuesday")
    createWeekDaySubjectTable(timeTable[2], Wednesday, "Wednesday")
    createWeekDaySubjectTable(timeTable[3], Thursday, "Thursday")
    createWeekDaySubjectTable(timeTable[4], Friday, "Friday")
    createWeekDaySubjectTable(timeTable[5], Saturday, "Saturday")

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
      if (breakTime(x) is Int) println("${breakTime(x) as Int - 0.5}, $y")
      when (val ret = breakTime(x)) {
        false -> return false
        is Int -> return when {
          ret < 10 -> (ret - 0.5 == y)
          else -> ((ret / 10).toDouble() == y)
        }
      }
      return false
    }

//    var even = true;
//    var j = 0
//    var k = 0
//    var t = 43000

    val calendar = Calendar.getInstance()
    val loopHandler = Handler(Looper.getMainLooper())
    loopHandler.post(object : Runnable {
      @SuppressLint("ResourceAsColor")
      override fun run() {

/*        val dayArray = arrayOf(
          Calendar.MONDAY,
          Calendar.TUESDAY,
          Calendar.WEDNESDAY,
          Calendar.THURSDAY,
          Calendar.FRIDAY,
          Calendar.SATURDAY,
          Calendar.SUNDAY)

        val timeArray = arrayOf(
          8 * 3600 + 0 * 60 + 0,
          8 * 3600 + 30 * 60 + 0,
          9 * 3600 + 15 * 60 + 0,
          9 * 3600 + 20 * 60 + 0,
          10 * 3600 + 5 * 60 + 0,
          10 * 3600 + 20 * 60 + 0,
          11 * 3600 + 5 * 60 + 0,
          11 * 3600 + 10 * 60 + 0,
          11 * 3600 + 55 * 60 + 0,
          12 * 3600 + 10 * 60 + 0,
          12 * 3600 + 55 * 60 + 0,
          13 * 3600 + 0 * 60 + 0,
          13 * 3600 + 45 * 60 + 0,
          14 * 3600 + 15 * 60 + 0,
          15 * 3600 + 0 * 60 + 0,
          15 * 3600 + 5 * 60 + 0,
          15 * 3600 + 50 * 60 + 0)

        val day = dayArray[k]
        if (j == timeArray.size - 1) {
          k++
          k %= dayArray.size
        }*/

        val day_of_week = calendar.get(Calendar.DAY_OF_WEEK)
        val current_year = calendar.get(Calendar.YEAR)
        val sept1 = LocalDate.of(current_year, 9, 1)
        val first_week_of_year = sept1.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
        val weeks_offset =
          LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR) - first_week_of_year
        var week = weeks_offset + 1
        if (day_of_week == Calendar.SUNDAY) week++ // Sunday is already in the next week
        val dnm = week % 2 == 0
        binding.TimeAndWeek.text =
          "week $week ${getTime()} ${if (dnm) "denominator" else "numerator"}"

        timeTable[0][1].text =
          "1. " + (if (dnm) "Physics 1-415 (exercise)" else "-")
        timeTable[1][2].text =
          "2. " + (if (dnm) "Machine-dependent PL 5-232 (lecture)" else "Philosophy 5-232 (lecture)")
        timeTable[2][4].text =
          "4. " + (if (dnm) "Discrete Mathematics 3-203 (lab)" else "-") // visibility
        timeTable[4][3].text =
          "3. " + (if (dnm) "Machine-dependent PL 5-222 (lab)" else "-")
        timeTable[4][5].text =
          "5. " + (if (dnm) "-" else "High-level programming 5-219 (lab)") // visibility
        timeTable[4][6].text =
          "6. " + (if (dnm) "-" else "High-level programming 5-219 (lab)") // visibility
        timeTable[5][1].text =
          "1. " + (if (dnm) "Fundamentals of electr. 5-231" else "High-level programming 5-219") + " (exercise)"
        timeTable[5][2].text =
          "2. " + (if (dnm) "-" else "Fundamentals of electronics 5-231 (lab)") // visibility
        if (dnm) {
          timeTable[2][4].visibility = View.VISIBLE
          timeTable[4][5].visibility = View.GONE
          timeTable[4][6].visibility = View.GONE
          timeTable[5][2].visibility = View.GONE
        } else {
          timeTable[2][4].visibility = View.GONE
          timeTable[4][5].visibility = View.VISIBLE
          timeTable[4][6].visibility = View.VISIBLE
          timeTable[5][2].visibility = View.VISIBLE
        }

//          t += 200
//          t %= 71000
//          t = max(t, 43000)
//        val t = timeArray[j]
//        j++
//        j %= timeArray.size
//        val t = 11 * 3600 + 54 * 60 + 0

        // Color Timetable
        val t = getTime("now", "int") as Int
        for ((weekDay, Day) in timeTable.withIndex()) {
          val defaultColor = (if (weekDay + 2 == day_of_week) yellow else green)
          var color = defaultColor
          for ((i, pair) in Day.withIndex()) {
            if (i == 0) {
              pair.setTextColor(getColor(color))
            } else {
              color = when {
                weekDay + 2 == day_of_week && studyTime(t, (i - 1) * 2) is Pair<*, *> -> red
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
//        var index: Any? = null
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
/*        when (index) {
          null -> index = getTime()
          else -> otherTime = false
        }*/


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

        loopHandler.postDelayed(this, 200)
      }
    })
  }
}
