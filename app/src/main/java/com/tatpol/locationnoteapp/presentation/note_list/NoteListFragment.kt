package com.tatpol.locationnoteapp.presentation.note_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.databinding.FragmentNoteListBinding
import com.tatpol.locationnoteapp.presentation.EventType
import com.tatpol.locationnoteapp.presentation.NoteEvent
import com.tatpol.locationnoteapp.presentation.adapter.NoteAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteListFragment : Fragment(), NoteAdapter.NoteItemClickListener {

    private val viewModel: NoteListViewModel by viewModels()

    private lateinit var binding: FragmentNoteListBinding

    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNoteListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteAdapter = NoteAdapter(requireContext(), this)
        binding.rvNoteList.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.notes.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Resource.Success -> {
                    noteAdapter.submitList(result.data)
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> {}
            }
        }
    }

    override fun onGetNoteRoute(note: Note) {
        val event = NoteEvent(note, EventType.SHOW_NOTE_ROUTE)
        setFragmentResult(NOTE_EVENT_REQUEST_KEY, bundleOf(NOTE_EVENT_BUNDLE_KEY to event))
        findNavController().navigate(NoteListFragmentDirections.actionNoteListFragmentToMapFragment())
    }

    override fun onEditNote(note: Note) {
        val event = NoteEvent(note, EventType.EDIT_NOTE)
        setFragmentResult(NOTE_EVENT_REQUEST_KEY, bundleOf(NOTE_EVENT_BUNDLE_KEY to event))
        findNavController().navigate(NoteListFragmentDirections.actionNoteListFragmentToCreateEditFragment())
    }

    override fun onDeleteNote(note: Note) {
        viewModel.deleteNote(note)
    }
}