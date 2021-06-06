package com.varyapps.nomorecats

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    private fun createNotificationChannel(){
        //TODO
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "1","Recognation notification",NotificationManager.IMPORTANCE_MIN
            ).apply {
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

    }

    //https://stackoverflow.com/questions/20136121/android-how-to-take-screenshot-programmatically
    //https://stackoverflow.com/questions/49620758/android-take-screenshot-of-notification-bar-programmatically
    //https://github.com/mtsahakis/MediaProjectionDemo
}