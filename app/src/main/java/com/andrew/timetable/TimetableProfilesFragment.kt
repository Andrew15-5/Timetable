package com.andrew.timetable

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.andrew.timetable.MainActivity.Companion.BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED
import com.andrew.timetable.databinding.FragmentTimetableProfilesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import com.andrew.timetable.Snackbar.Companion as ThemedSnackbar

class TimetableProfilesFragment : Fragment() {
  private lateinit var binding: FragmentTimetableProfilesBinding
  private lateinit var timetable_profileDAO: TimetableProfileDAO
  private var profiles: List<TimetableProfile> = listOf()
  private lateinit var edit_launcher: ActivityResultLauncher<Intent>
  private lateinit var edit_intent: Intent

  val broadcast_receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED) {
        update_profiles()
      }
    }
  }

  private val on_delete_button_click = fun(name: String): View.OnClickListener {
    return View.OnClickListener {
      lifecycleScope.launch {
        timetable_profileDAO.delete(name)
        update_profiles()
      }
    }
  }

  private lateinit var on_export_button_click:
    (TimetableProfile) -> View.OnClickListener

  private val on_edit_button_click =
    fun(timetable_profile: TimetableProfile): View.OnClickListener {
      return View.OnClickListener {
        val intent = Intent(edit_intent)
        intent.putExtra("name", timetable_profile.name)
        intent.putExtra("timetable", timetable_profile.timetable.toString())
        edit_launcher.launch(intent)
      }
    }

  private fun update_profiles() {
    lifecycleScope.launch {
      profiles = timetable_profileDAO.get_all()
      if (profiles.isEmpty()) {
        binding.timetableProfilesListView.visibility = View.GONE
        binding.noTimetableProfiles.visibility = View.VISIBLE
        return@launch
      }
      binding.timetableProfilesListView.visibility = View.VISIBLE
      binding.noTimetableProfiles.visibility = View.GONE
      binding.timetableProfilesListView.adapter =
        TimetableProfileAdapter(
          requireActivity(),
          profiles,
          on_delete_button_click,
          on_export_button_click,
          on_edit_button_click,
        )
    }
  }

  override fun onResume() {
    super.onResume()
    val intent_filter =
      IntentFilter(BROADCAST_ACTION_TIMETABLE_PROFILES_UPDATED)
    LocalBroadcastManager.getInstance(requireContext())
      .registerReceiver(broadcast_receiver, intent_filter)
  }

  override fun onPause() {
    LocalBroadcastManager.getInstance(requireContext())
      .unregisterReceiver(broadcast_receiver)
    super.onPause()
  }

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
    val activity = activity as MainActivity

    on_export_button_click =
      fun(timetable_profile: TimetableProfile): View.OnClickListener {
        return View.OnClickListener {
          activity.backup_timetable_profile(timetable_profile)
        }
      }

    val cancel_message = ThemedSnackbar.make(
      activity,
      binding.root,
      "Editing was canceled, no changes were saved",
      Snackbar.LENGTH_LONG
    )

    val ok_message = ThemedSnackbar.make(
      activity,
      binding.root,
      "Timetable profile was updated",
      Snackbar.LENGTH_SHORT
    )

    edit_intent = Intent(activity, EditTimetableProfileActivity::class.java)
    edit_launcher = registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) {
      if (it.resultCode == RESULT_OK) {
        ok_message.show()
        update_profiles()
      } else if (it.resultCode == RESULT_CANCELED) {
        cancel_message.show()
      }
    }

    binding.importTimetableButton.setOnClickListener {
      activity.import_timetable_profile()
    }
    lifecycleScope.launch {
      val db = DatabaseHelper.instance(activity)
      timetable_profileDAO = db.timetable_profileDAO()
      update_profiles()
    }
  }
}