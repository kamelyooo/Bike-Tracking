package com.example.biketrackingapp.ui.viewModels

import android.annotation.SuppressLint
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biketrackingapp.db.Run
import com.example.biketrackingapp.other.SortType
import com.example.biketrackingapp.repos.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("LogNotTimber")
@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepo: MainRepo
) : ViewModel() {
    private val getRunSortedByDate = mainRepo.getAllRunsSortedByDate()
    private val getRunSortedByRunningTime = mainRepo.getAllRunsSortedByTimeInMills()
    private val getRunSortedByDistance = mainRepo.getAllRunsSortedByDistance()
    private val getRunSortedByAvgSpeed = mainRepo.getAllRunsSortedByAVGSpeed()
    private val getRunSortedByCaloriesBurned = mainRepo.getAllRunsSortedByCaloriesBurned()
    val runs = MediatorLiveData<List<Run>>()
    var sortType = SortType.DATE

    init {
        runs.addSource(getRunSortedByDate) {
            if (sortType == SortType.DATE) {
                it?.let {
                    runs.value = it
                }
            }

        }
        runs.addSource(getRunSortedByRunningTime) {
        }
        runs.addSource(getRunSortedByDistance) {

        }
        runs.addSource(getRunSortedByAvgSpeed) {

        }
        runs.addSource(getRunSortedByCaloriesBurned) {
        }
    }


    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> getRunSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> getRunSortedByRunningTime.value?.let { runs.value = it }
        SortType.AVG_SPEED -> getRunSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> getRunSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> getRunSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }


    fun insert(run: Run) = viewModelScope.launch {
        mainRepo.insertRun(run)
    }
}