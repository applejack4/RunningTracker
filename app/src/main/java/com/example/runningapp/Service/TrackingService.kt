package com.example.runningapp.Service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.other.Constants.NOTIFICATION_ID
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias polyLine = MutableList<LatLng>
typealias polyLines = MutableList<polyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun : Boolean = true

    @Inject
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    private lateinit var baseNotificationBuilder : NotificationCompat.Builder

    lateinit var currNotificationBuilder : NotificationCompat.Builder

    private val timeRunInSeconds = MutableLiveData<Long>()

    private var isTimeEnabled = false

    private var lapTime = 0L

    private var timeRun = 0L

    private var timeStarted = 0L

    private var lastSecondTimeStamp = 0L

    var serviceKilled : Boolean = false

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<polyLines>()
    }

    override fun onCreate() {
        super.onCreate()
        currNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Stop" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        }else{
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //this is to remove all the actions before you update any action on notifications
        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled){
            currNotificationBuilder = baseNotificationBuilder.addAction(R.drawable.ic_pause, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currNotificationBuilder.build())
        }
    }

    private fun startTime(){
        addEmptyPolyLines()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimeEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                //time difference between now and the time started.
                lapTime = System.currentTimeMillis() - timeStarted
                //post the new lap time.
                timeRunInMillis.postValue(timeRun + lapTime)
                if(timeRunInMillis.value!! >=  lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
        }
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
                when(it.action){
                    ACTION_START_OR_RESUME_SERVICE -> {
                        if(isFirstRun){
                            startForeGroundService()
                            isFirstRun = false
                        }else{
                            Timber.d("Activity Resumed.")
                            startTime()
                        }
                    }

                    ACTION_PAUSE_SERVICE ->{
                        pauseService()
                    }

                    ACTION_STOP_SERVICE ->{
                        Timber.d("Stop")
                        killService()
                    }
                }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if(isTracking){
            if(TrackingUtility.requestPermissions(this)){
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallBacks,
                    Looper.getMainLooper())
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallBacks)
        }
    }

    val locationCallBacks = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result.locations.let { locations ->
                    for (location in locations){
                        addPathPoints(location)
                    }
                }
            }
        }
    }

    private fun addPathPoints(location : Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyLines() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForeGroundService(){
        startTime()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled){
                val notification = currNotificationBuilder.setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
     val channel = NotificationChannel(
         NOTIFICATION_CHANNEL_ID,
         NOTIFICATION_CHANNEL_NAME,
         IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT)

    private fun pauseService(){
        isTracking.postValue(false)
        isTimeEnabled = false
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
}