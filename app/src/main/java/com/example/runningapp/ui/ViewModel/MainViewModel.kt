package com.example.runningapp.ui.ViewModel

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.other.SortType
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repo : MainRepository) : ViewModel(){

    fun insertRun(run : Run) = viewModelScope.launch {
        repo.insert(run)
    }

    private val runSortBuDate = repo.getRunsSortedByDate()

    private val runSortBuDistance = repo.getRunsSortedByDistance()

    private val runSortByCaloriesBurned = repo.getRunsSortedByCaloriesBurned()

    private val runSortByTime = repo.getRunsSortedByTime()

    private val runSortByAverageSpeed = repo.getRunsSortedByAverageSpeed()

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runSortBuDate){ result ->
            if(sortType == SortType.DATE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortBuDistance){ result ->
            if(sortType == SortType.DISTANCE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortByAverageSpeed){ result ->
            if(sortType == SortType.AVG_SPEED){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortByCaloriesBurned){ result ->
            if(sortType == SortType.CALORIES_BURNED){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runSortByTime){ result ->
            if(sortType == SortType.RUNNING_TIME){
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType){
        SortType.DATE -> runSortBuDate.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortBuDate.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortBuDate.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortBuDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortBuDate.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }



}