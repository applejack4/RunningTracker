package com.example.runningapp.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.runningapp.Service.polyLine
import com.google.android.gms.maps.model.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    fun requestPermissions(context : Context) =
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }else{
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

    fun getFormattedStopWatchTime(ms : Long, includeMillis : Boolean = false) : String {
        var milliSecs = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliSecs)
        milliSecs -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms)
        milliSecs -= TimeUnit.MINUTES.toMillis(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours" +
                    "${if(minutes < 10) "0" else ""}$minutes" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliSecs -= TimeUnit.SECONDS.toMillis(hours)
        milliSecs /= 10

        return "${if(hours < 10) "0" else ""}$hours" +
                "${if(minutes < 10) "0" else ""}$minutes" +
                "${if(seconds < 10) "0" else ""}$seconds" +
                "${if(milliSecs < 10) "0" else ""}$milliSecs"
    }

    fun calculatePolyLineLength(polyLine: polyLine) : Float{
        var distance = 0f
        for(i in 0..polyLine.size - 2){
            val pos1 = polyLine[i]
            val pos2 = polyLine[i + 1]

            val result = FloatArray(1)
            Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, result)
            distance += result[0]
        }
        return distance
    }
}