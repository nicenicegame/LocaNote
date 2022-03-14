package com.tatpol.locationnoteapp.presentation.create_edit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_BUNDLE_KEY
import com.tatpol.locationnoteapp.Constants.NOTE_EVENT_REQUEST_KEY
import com.tatpol.locationnoteapp.databinding.FragmentCreateEditBinding
import com.tatpol.locationnoteapp.presentation.MapNoteViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "CreateEditFragment"

@AndroidEntryPoint
class CreateEditFragment : Fragment() {

    private val viewModel: MapNoteViewModel by activityViewModels()

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
        setFragmentResultListener(NOTE_EVENT_REQUEST_KEY) { _, bundle ->
            val result = bundle.get(NOTE_EVENT_BUNDLE_KEY)
            Log.d(TAG, result.toString())
        }
        subscribeUi()
    }

    private fun subscribeUi() {
        viewModel.lastKnownLocation.observe(viewLifecycleOwner) { location ->
            location?.let {
                binding.tvLatLng.text = "${it.latitude}, ${it.longitude}"
            }
        }
        viewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            address?.let {
                binding.tvAddress.text = it
            }
        }
    }
}