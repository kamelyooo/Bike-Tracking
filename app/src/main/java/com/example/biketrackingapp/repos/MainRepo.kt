package com.example.biketrackingapp.repos

import com.example.biketrackingapp.db.Run
import com.example.biketrackingapp.db.RunDao
import javax.inject.Inject

class MainRepo @Inject constructor(
    val RunDao: RunDao
) {

    suspend fun insertRun(run: Run) = RunDao.insertRun(run)
    suspend fun deleteRun(run: Run) = RunDao.deleteRun(run)
    fun getAllRunsSortedByDate() = RunDao.getAllRunsSortedByDate()
    fun getAllRunsSortedByDistance() = RunDao.getAllRunsSortedByDistance()
    fun getAllRunsSortedByTimeInMills() = RunDao.getAllRunsSortedByTimeInMillis()
    fun getAllRunsSortedByAVGSpeed() = RunDao.getAllRunsSortedByAvgSpeed()
    fun getAllRunsSortedByCaloriesBurned() = RunDao.getAllRunsSortedByCaloriesBurned()
    fun getTotalAvgSpeed() = RunDao.getTotalAvgSpeed()
    fun getTotalDistance() = RunDao.getTotalDistance()
    fun getTotalCaloriesBurned() = RunDao.getTotalCaloriesBurned()
    fun getTotalTimeInMills() = RunDao.getTotalTimeMillis()

}