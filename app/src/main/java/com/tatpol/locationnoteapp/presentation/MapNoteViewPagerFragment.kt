package com.tatpol.locationnoteapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.Constants.OPEN_SETTINGS_REQUEST_KEY
import com.tatpol.locationnoteapp.Constants.SNACKBAR_REQUEST_KEY
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

    private lateinit var binding: FragmentMapNoteViewPagerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapNoteViewPagerBinding.inflate(inflater)
        val bottomNavigation = binding.bottomNavigation
        val viewPager = binding.viewPager

        setUpViewPager(viewPager, bottomNavigation)
        setUpBottomNavListener(bottomNavigation, viewPager)
        listenToFragmentResult(viewPager, bottomNavigation)
        subscribeUi(viewPager, binding, bottomNavigation)

        return binding.root
    }

    private fun subscribeUi(
        viewPager: ViewPager2,
        binding: FragmentMapNoteViewPagerBinding,
        bottomNavigation: BottomNavigationView
    ) {
        viewModel.createEditFormEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is FormEvent.Success -> {
                    viewPager.currentItem = NOTE_LIST_PAGE_INDEX
                    event.message?.let {
                        Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT)
                            .setAnchorView(bottomNavigation)
                            .show()
                    }
                }
                else -> Unit
            }
        }
    }

    private fun listenToFragmentResult(
        viewPager: ViewPager2,
        bottomNavigation: BottomNavigationView
    ) {
        childFragmentManager.setFragmentResultListener(
            NOTE_EVENT_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val noteEvent = bundle.get(NOTE_EVENT_BUNDLE_KEY) as NoteEvent
            when (noteEvent.type) {
                EventType.EDIT_NOTE -> {
                    viewPager.currentItem = CREATE_EDIT_PAGE_INDEX
                    viewModel.setFormMode(FormMode.EditMode(noteEvent.note!!))
                }
                EventType.SHOW_NOTE_ROUTE -> {
                    viewPager.currentItem = MAP_PAGE_INDEX
                    viewModel.setMapMode(MapMode.RoutingMode(noteEvent.note!!))
                }
            }
        }
        childFragmentManager.setFragmentResultListener(
            SNACKBAR_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, _ ->
            Snackbar.make(
                binding.root,
                "Location permissions need to be granted. Open application settings?",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Open") {
                    childFragmentManager.setFragmentResult(
                        OPEN_SETTINGS_REQUEST_KEY,
                        bundleOf()
                    )
                }
                .setAnchorView(bottomNavigation)
                .show()
        }
    }

    private fun setUpViewPager(
        viewPager: ViewPager2,
        bottomNavigation: BottomNavigationView
    ) {
        viewPager.adapter = MapNotePagerAdapter(this)
        viewPager.isUserInputEnabled = false

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    MAP_PAGE_INDEX -> bottomNavigation.selectedItemId = R.id.mapFragment
                    NOTE_LIST_PAGE_INDEX -> bottomNavigation.selectedItemId = R.id.noteListFragment
                    CREATE_EDIT_PAGE_INDEX -> bottomNavigation.selectedItemId =
                        R.id.createEditFragment
                }
            }
        })
    }

    private fun setUpBottomNavListener(
        bottomNavigation: BottomNavigationView,
        viewPager: ViewPager2
    ) {
        bottomNavigation.setOnItemSelectedListener { menu ->
            viewModel.setFormMode(FormMode.CreateMode)
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
    }
}