package com.example.runningtrackerapp.repositories

import com.example.runningtrackerapp.db.Run
import com.example.runningtrackerapp.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByDuration() = runDao.getAllRunsSortedByDuration()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeedInKMH() = runDao.getTotalAvgSpeedInKMH()

    fun getTotalTotalDistance() = runDao.getTotalTotalDistance()

    fun getTotalDurationInMillis() = runDao.getTotalDurationInMillis()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()
}