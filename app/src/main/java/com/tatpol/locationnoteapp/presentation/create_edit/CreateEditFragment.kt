package com.tatpol.locationnoteapp.presentation.create_edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.tatpol.locationnoteapp.databinding.FragmentCreateEditBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateEditFragment : Fragment() {

    private val viewModel: CreateEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCreateEditBinding.inflate(inflater)

        return binding.root
    }
}