package com.tatpol.locationnoteapp.presentation.create_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.tatpol.locationnoteapp.Constants
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.R
import com.tatpol.locationnoteapp.databinding.FragmentCreateEditBinding
import com.tatpol.locationnoteapp.presentation.EventType
import com.tatpol.locationnoteapp.presentation.MapNoteViewModel
import com.tatpol.locationnoteapp.presentation.NoteEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateEditFragment : Fragment() {

    private val viewModel: MapNoteViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

    private lateinit var binding: FragmentCreateEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmit.setOnClickListener {
            val title = binding.etNoteTitle.editText?.text.toString()
            val description = binding.etNoteDescription.editText?.text.toString()
            viewModel.submitForm(title, description)
        }

        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            if (viewModel.formMode.value is FormMode.CreateMode)
                binding.etAddress.editText?.setText(address)
        }
        viewModel.formMode.observe(viewLifecycleOwner) { formMode ->
            when (formMode) {
                is FormMode.CreateMode -> {
                    binding.apply {
                        topAppBar.title = getString(R.string.create_note_title)
                        etNoteTitle.editText?.text?.clear()
                        etNoteDescription.editText?.text?.clear()
                        etAddress.editText?.setText(viewModel.currentAddress.value)
                    }
                }
                is FormMode.EditMode -> {
                    binding.apply {
                        topAppBar.title = getString(R.string.edit_note_title)
                        etNoteTitle.editText?.setText(formMode.note.title)
                        etNoteDescription.editText?.setText(formMode.note.description)
                        etAddress.editText?.setText(formMode.note.address)
                    }
                }
            }
        }
    }
}