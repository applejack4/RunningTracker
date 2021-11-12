package com.example.runningapp.ui.ViewModel

import androidx.lifecycle.ViewModel
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(val repo : MainRepository) : ViewModel(){

    val totalTimeRun = repo.getTotalTimeMills()
    val totalCaloriesBurned = repo.getTotalCaloriesBurned()
    val totalAverageSpeed = repo.getTotalAverageSpeed()
    val totalDistanceRun = repo.getTotalDistanceInMeter()

    val sortRunsByDate = repo.getRunsSortedByDate()


}