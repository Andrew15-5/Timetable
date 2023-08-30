package com.andrew.timetable

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.andrew.timetable.databinding.ActivityEditTimetableProfileBinding
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class EditTimetableProfileActivity : AppCompatActivity() {
  private lateinit var binding: ActivityEditTimetableProfileBinding

  private fun cancel() {
    setResult(RESULT_CANCELED)
    finish()
  }

  private fun ok() {
    setResult(RESULT_OK)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityEditTimetableProfileBinding.inflate(layoutInflater)
    setContentView(binding.root)

    val name = intent.getStringExtra("name")
    val timetable = intent.getStringExtra("timetable")
    if (name == null || timetable == null) {
      setResult(RESULT_CANCELED)
      finish()
      return
    }

    val timetable_profile = TimetableProfile(name, JSONObject(timetable))

    val reset_fields = {
      binding.nameEditText.setText(timetable_profile.name)
      binding.timetableEditText.setText(timetable_profile.pretty_timetable())
    }

    reset_fields()

    binding.resetButton.setOnClickListener { reset_fields() }

    binding.cancelButton.setOnClickListener { cancel() }

    binding.saveButton.setOnClickListener {
      val new_timetable_profile =
        get_timetable_profile() ?: return@setOnClickListener

      if (new_timetable_profile.name == timetable_profile.name &&
        new_timetable_profile.timetable.toString() ==
        timetable_profile.timetable.toString()
      ) cancel()

      lifecycleScope.launch {
        val db = DatabaseHelper.instance(this@EditTimetableProfileActivity)
        val timetable_profileDAO = db.timetable_profileDAO()
        if (timetable_profile.name == new_timetable_profile.name) {
          timetable_profileDAO.update(new_timetable_profile)
        } else {
          timetable_profileDAO.delete(timetable_profile)
          timetable_profileDAO.insert(new_timetable_profile)
        }
        ok()
      }
    }
  }

  fun get_timetable_profile(): TimetableProfile? {
    val name = binding.nameEditText.text.toString()
    val timetable = binding.timetableEditText.text.toString()
    val empty_name = "Name can't be empty"
    val empty_timetable = "Timetable can't be empty"
    val invalid_timetable = "Timetable is not a valid JSON string"
    if (name.isEmpty()) {
      binding.errorMessageTextView.text = empty_name
      return null
    }
    if (timetable.isEmpty()) {
      binding.errorMessageTextView.text = empty_timetable
      return null
    }
    return try {
      TimetableProfile(name, JSONObject(timetable))
    } catch (exception: JSONException) {
      binding.errorMessageTextView.text = invalid_timetable
      null
    }
  }

  override fun onKeyDown(key_code: Int, event: KeyEvent?): Boolean {
    if (key_code == KeyEvent.KEYCODE_BACK) {
      cancel()
      return true
    }
    return super.onKeyDown(key_code, event)
  }
}