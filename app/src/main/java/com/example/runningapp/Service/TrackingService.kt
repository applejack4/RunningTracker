package com.example.runningapp.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.runningapp.R
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import timber.log.Timber

class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
                when(it.action){
                    ACTION_START_OR_RESUME_SERVICE -> {
                        Timber.d("started or resume")
                    }

                    ACTION_PAUSE_SERVICE ->{
                        Timber.d("Pause")
                    }

                    ACTION_STOP_SERVICE ->{
                        Timber.d("Stop")
                    }
                }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeGroundService(){
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false).setOngoing(true).
            setSmallIcon(R.drawable.ic_direction_run)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
     val channel = NotificationChannel(
         NOTIFICATION_CHANNEL_ID,
         NOTIFICATION_CHANNEL_NAME,
         IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}