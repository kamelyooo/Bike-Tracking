package com.example.biketrackingapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.location.Location

import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.biketrackingapp.R
import com.example.biketrackingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.biketrackingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.biketrackingapp.other.Constants.ACTION_STOP_SERVICE
import com.example.biketrackingapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.biketrackingapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.biketrackingapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.biketrackingapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.biketrackingapp.other.Constants.NOTIFICATION_ID
import com.example.biketrackingapp.other.TrakingUtility
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

typealias polyLine = MutableList<LatLng>
typealias PolyLines = MutableList<polyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val timeRunINSecond = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var notificationManager: NotificationManager

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLines>(mutableListOf(mutableListOf()))
        var timeRunInMillis = MutableLiveData<Long>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf(mutableListOf()))
        timeRunINSecond.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        isTracking.observe(this, Observer {
            updateTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "pause" else "Resume"
        val pendingIntent = if (isTracking) {
            PendingIntent.getService(
                this,
                1,
                Intent(this, TrackingService::class.java).also {
                    it.action = ACTION_PAUSE_SERVICE
                },
                FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(
                this,
                2,
                Intent(this, TrackingService::class.java).also {
                    it.action = ACTION_START_OR_RESUME_SERVICE
                },
                FLAG_UPDATE_CURRENT
            )
        }


        baseNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(baseNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            baseNotificationBuilder.addAction(
                R.drawable.ic_pause_black_24dp,
                notificationActionText,
                pendingIntent
            )
            notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.build())
        }

    }

    private var itTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimesStamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        itTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {

                //time difference between now and time started
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimesStamp + 1000L) {
                    timeRunINSecond.postValue(timeRunINSecond.value!! + 1)
                    lastSecondTimesStamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
            Timber.d("timeRun: $timeRun ")
        }
    }


    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(it.latitude, it.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrakingUtility.hasLocationPermissions(this)) {

                    val request = LocationRequest().apply {
                        interval = LOCATION_UPDATE_INTERVAL
                        fastestInterval = FASTEST_LOCATION_INTERVAL
                        priority = PRIORITY_HIGH_ACCURACY
                    }
                    fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                    )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }



    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeRunInMillis.postValue(0L)
    }
    private fun killService() {
        isFirstRun = true
        serviceKilled = true
        postInitialValues()
        pauseTracking()
        stopForeground(true)
        stopSelf()


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForGroundService()
                        isFirstRun = false
                    } else
                        startTimer()
                }
                ACTION_PAUSE_SERVICE -> {
                    pauseTracking()
                }
                ACTION_STOP_SERVICE -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseTracking() {
        isTracking.postValue(false)
        itTimerEnabled = false
    }


    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    private fun startForGroundService() {
        startTimer()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(notificationManager)

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunINSecond.observe(this, Observer { long ->
            if (!serviceKilled) {
                notificationManager.notify(NOTIFICATION_ID, baseNotificationBuilder.let {
                    it.setContentText(TrakingUtility.getFormattedStopWatchTime(long * 1000L))
                    it.build()
                })
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}