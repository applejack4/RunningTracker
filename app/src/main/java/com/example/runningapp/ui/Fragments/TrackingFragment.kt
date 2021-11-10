package com.example.runningapp.ui.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.runningapp.R
import com.example.runningapp.Service.TrackingService
import com.example.runningapp.databinding.FragmentTrackingBinding
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.ui.ViewModel.MainViewModel
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private val viewModel : MainViewModel by viewModels()

    private lateinit var _binding : FragmentTrackingBinding
    private var map : GoogleMap ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.mapView.onCreate(savedInstanceState)
        _binding.mapView.getMapAsync {
            map = it
        }

        _binding.btnToggleRun.setOnClickListener {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onResume() {
        super.onResume()
        _binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        _binding.mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        _binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        _binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding.mapView.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding.mapView.onSaveInstanceState(outState)
    }


    private fun sendCommandToService(action : String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
        }
}