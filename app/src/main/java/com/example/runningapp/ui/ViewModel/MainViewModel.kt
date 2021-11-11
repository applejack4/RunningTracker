package com.example.runningapp.ui.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val repo : MainRepository) : ViewModel(){

    fun insertRun(run : Run) = viewModelScope.launch {
        repo.insert(run)
    }
}