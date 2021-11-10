package com.example.runningapp.repositories

import com.example.runningapp.db.Run
import com.example.runningapp.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(private val runDao : RunDao) {

    suspend fun insert(run : Run) = runDao.insertRun(run)

    suspend fun delete(run : Run ) = runDao.deleteRun(run)

    fun getRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getRunsSortedByTime() = runDao.getAllRunsSortedByTime()

    fun getRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getRunsSortedByAverageSpeed() = runDao.getAllRunsSortedByAverageSpeed()

    fun getRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getTotalTimeMills() = runDao.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalDistanceInMeter() = runDao.getTotalDistanceInMeter()

    fun getTotalAverageSpeed() = runDao.getTotalAverageSpeed()
}