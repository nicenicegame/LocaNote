package com.tatpol.locationnoteapp.presentation.note_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tatpol.locationnoteapp.data.model.Note
import com.tatpol.locationnoteapp.data.model.Resource
import com.tatpol.locationnoteapp.databinding.FragmentNoteListBinding
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

        noteAdapter = NoteAdapter(requireContext(), this)
        binding.rvNoteList.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        subscribeUi()

        return binding.root
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
        TODO("Not yet implemented")
    }

    override fun onEditNote(note: Note) {
        TODO("Not yet implemented")
    }

    override fun onDeleteNote(note: Note) {
        viewModel.deleteNote(note)
    }
}