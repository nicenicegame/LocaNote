package com.tatpol.locationnoteapp.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tatpol.locationnoteapp.presentation.create_edit.CreateEditFragment
import com.tatpol.locationnoteapp.presentation.map.MapFragment
import com.tatpol.locationnoteapp.presentation.note_list.NoteListFragment

const val MAP_PAGE_INDEX = 0
const val NOTE_LIST_PAGE_INDEX = 1
const val CREATE_EDIT_PAGE_INDEX = 2

class MapNotePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        MAP_PAGE_INDEX to { MapFragment() },
        NOTE_LIST_PAGE_INDEX to { NoteListFragment() },
        CREATE_EDIT_PAGE_INDEX to { CreateEditFragment() }
    )

    override fun getItemCount(): Int {
        return tabFragmentsCreators.size
    }

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}