package com.tatpol.locationnoteapp.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.databinding.FragmentMapNoteViewPagerBinding
import com.tatpol.locationnoteapp.presentation.adapter.CREATE_EDIT_PAGE_INDEX
import com.tatpol.locationnoteapp.presentation.adapter.MAP_PAGE_INDEX
import com.tatpol.locationnoteapp.presentation.adapter.MapNotePagerAdapter
import com.tatpol.locationnoteapp.presentation.adapter.NOTE_LIST_PAGE_INDEX
import com.tatpol.locationnoteapp.presentation.create_edit.FormMode
import com.tatpol.locationnoteapp.presentation.map.MapMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapNoteViewPagerFragment : Fragment() {

    private val viewModel: MapNoteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapNoteViewPagerBinding.inflate(inflater)
        val bottomNavigation = binding.bottomNavigation
        val viewPager = binding.viewPager

        viewPager.adapter = MapNotePagerAdapter(this)
        viewPager.isUserInputEnabled = false

        bottomNavigation.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.mapFragment -> {
                    viewPager.currentItem = MAP_PAGE_INDEX
                    true
                }
                R.id.noteListFragment -> {
                    viewPager.currentItem = NOTE_LIST_PAGE_INDEX
                    true
                }
                R.id.createEditFragment -> {
                    viewPager.currentItem = CREATE_EDIT_PAGE_INDEX
                    true
                }
                else -> false
            }
        }

        childFragmentManager.setFragmentResultListener(
            NOTE_EVENT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val noteEvent = bundle.get(NOTE_EVENT_BUNDLE_KEY) as NoteEvent
            when (noteEvent.type) {
                EventType.EDIT_NOTE -> {
                    viewPager.currentItem = CREATE_EDIT_PAGE_INDEX
                    viewModel.setFormMode(FormMode.EditMode(noteEvent.note))
                }
                EventType.SHOW_NOTE_ROUTE -> {
                    viewPager.currentItem = MAP_PAGE_INDEX
                    viewModel.setMapMode(MapMode.RoutingMode(noteEvent.note))
                }
            }
        }

        Log.d("MapNote", viewModel.toString())

        return binding.root
    }
}