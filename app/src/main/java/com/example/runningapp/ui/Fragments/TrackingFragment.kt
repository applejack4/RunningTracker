package com.example.runningapp.ui.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.Service.TrackingService
import com.example.runningapp.Service.polyLine
import com.example.runningapp.databinding.FragmentTrackingBinding
import com.example.runningapp.db.Run
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.ViewModel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var isTracking = false
    private var pathPoints = mutableListOf<polyLine>()

    private val viewModel : MainViewModel by viewModels()

    private lateinit var _binding : FragmentTrackingBinding
    private var map : GoogleMap ?= null

    private var curTimeInMillis = 0L

    private var menu : Menu ?= null

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentTrackingBinding.inflate(layoutInflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.mapView.onCreate(savedInstanceState)

        _binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        _binding.mapView.getMapAsync {
            map = it
            addPolyLines()
        }

        _binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        subscribeToObserver()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_trackig_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Are you Sure, You want to cancel the run")
            .setIcon(R.drawable.ic_delete).setPositiveButton("Yes"){ _, _ ->
                stopRun()
            }.setNegativeButton("No"){dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()

        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
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
            menu?.getItem(0)?.isVisible = true
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
            menu?.getItem(0)?.isVisible = true
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

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        for (polyLines in pathPoints){
            for(pos in polyLines){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                _binding.mapView.width,
                _binding.mapView.height,
                (_binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDB(){
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyLines in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyLines).toInt()
            }
            val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60)) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimeStamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Toast.makeText(requireContext(), "Run Added successfully.", Toast.LENGTH_LONG).show()
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