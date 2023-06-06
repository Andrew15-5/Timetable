package com.andrew.timetable

import android.content.Context
import android.util.TypedValue
import android.view.View
import com.google.android.material.snackbar.Snackbar

class Snackbar {
  companion object {
    const val LENGTH_SHORT = Snackbar.LENGTH_SHORT
    const val LENGTH_LONG = Snackbar.LENGTH_LONG
    const val LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE

    fun make(
      context: Context,
      view: View,
      text: CharSequence,
      duration: Int,
    ): Snackbar {
      val typed_value = TypedValue()
      context.theme.resolveAttribute(R.attr.colorOnPrimary, typed_value, true)
      val text_color = typed_value.data
      val background_color = context.getColor(R.color.snackbar)
      val snack_bar = Snackbar
        .make(view, text, duration)
        .setTextColor(text_color)
      snack_bar.view.setBackgroundColor(background_color)
      return snack_bar
    }
  }
}