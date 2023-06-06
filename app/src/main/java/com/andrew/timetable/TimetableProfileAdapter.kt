package com.andrew.timetable

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.andrew.timetable.databinding.TimetableProfileListItemBinding

class TimetableProfileAdapter(
  private val context: Context,
  private val items: List<TimetableProfile>,
) : BaseAdapter() {

  override fun getCount(): Int {
    return items.size
  }

  override fun getItem(position: Int): Any {
    return items[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(
    position: Int,
    convert_view: View?,
    parent: ViewGroup?,
  ): View {
    val binding: TimetableProfileListItemBinding
    val item_view: View
    if (convert_view == null) {
      binding = TimetableProfileListItemBinding.inflate(
        LayoutInflater.from(context),
        parent,
        false
      )
      item_view = binding.root
    } else {
      binding = convert_view.tag as TimetableProfileListItemBinding
      item_view = convert_view
    }
    item_view.tag = binding
    val item = items[position]
    binding.name.text = item.name
    return item_view
  }
}