package com.example.runningapp.ui.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.Service.TrackingService
import com.example.runningapp.Service.polyLine
import com.example.runningapp.databinding.FragmentTrackingBinding
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.ViewModel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var isTracking = false
    private var pathPoints = mutableListOf<polyLine>()

    private val viewModel : MainViewModel by viewModels()

    private lateinit var _binding : FragmentTrackingBinding
    private var map : GoogleMap ?= null

    private var curTimeInMillis = 0L

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
            addPolyLines()
        }

        _binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyLines()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis)
            _binding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTracking(isTracking : Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            _binding.btnToggleRun.text = "Start"
            _binding.btnFinishRun.visibility = View.VISIBLE
        }else{
            _binding.btnToggleRun.text = "Stop"
            _binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pathPoints.last().last(), 15f
            ))
        }
    }

    private fun addPolyLines(){
        for (polyLine in pathPoints){
            val polyLineOptions = PolylineOptions()
                .color(Color.RED)
                .width(8f)
                .addAll(polyLine)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addLatestPolyLines(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng  = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLan = pathPoints.last().last()
            val polyLineOptions = PolylineOptions().color(Color.RED).width(8f).add(preLastLatLng).add(lastLatLan)
            map?.addPolyline(polyLineOptions)
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