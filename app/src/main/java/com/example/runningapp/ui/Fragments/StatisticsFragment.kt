package com.example.runningapp.ui.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.databinding.FragmentStatisticsBinding
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.ViewModel.StatisticViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel : StatisticViewModel by viewModels()

    private var _binding : FragmentStatisticsBinding ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(layoutInflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserver()
    }

    private fun subscribeToObserver(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                _binding!!.tvTotalTime.text = totalTimeRun

                viewModel.totalDistanceRun.observe(viewLifecycleOwner, Observer {
                    it?.let {
                        val km = it / 1000f
                        val totalDistance = round(km * 10f) / 10f
                        val totalDistanceString = "${totalDistance}km"
                        _binding!!.tvTotalDistance.text = totalDistanceString
                    }
                })
                viewModel.totalAverageSpeed.observe(viewLifecycleOwner, Observer {
                    it?.let {
                        val avgSpeed = round(it * 10f) / 10f
                        val avgSpeedString = "${avgSpeed}km/h"
                        _binding!!.tvAverageSpeed.text = avgSpeedString
                    }
                })
                viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
                    it?.let {
                        val totalCalories = "${it}kcal"
                        _binding!!.tvTotalCalories.text = totalCalories
                    }
                })
            } ?: return@Observer
        })
    }

}