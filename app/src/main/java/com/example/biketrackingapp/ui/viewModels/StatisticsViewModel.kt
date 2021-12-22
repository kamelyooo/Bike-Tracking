package com.example.biketrackingapp.ui.viewModels

import androidx.lifecycle.ViewModel
import com.example.biketrackingapp.repos.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepo:MainRepo
):ViewModel(){
    val totalTimeRun=mainRepo.getTotalTimeInMills()
    val totalDistanceInMeters=mainRepo.getTotalDistance()
    val totalAvGSpeed=mainRepo.getTotalAvgSpeed()
    val totalCaloriesBurned=mainRepo.getTotalCaloriesBurned()

    val getAllRunsSortedByDate=mainRepo.getAllRunsSortedByDate()
}