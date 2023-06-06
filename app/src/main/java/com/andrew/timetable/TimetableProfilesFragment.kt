package com.andrew.timetable

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.andrew.timetable.databinding.FragmentTimetableProfilesBinding

class TimetableProfilesFragment : Fragment() {
  private lateinit var binding: FragmentTimetableProfilesBinding

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (activity as MainActivity).timetable_profiles_menu_item_visibility(false)
  }

  override fun onDetach() {
    super.onDetach()
    (activity as MainActivity).timetable_profiles_menu_item_visibility(true)
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?,
  ): View {
    binding =
      FragmentTimetableProfilesBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.timetableProfilesListView
  }
}