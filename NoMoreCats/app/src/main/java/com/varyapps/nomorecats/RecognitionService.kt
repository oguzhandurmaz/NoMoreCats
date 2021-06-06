package com.varyapps.nomorecats

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup.LayoutParams.FILL_PARENT
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.view.WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class RecognitionService : Service() {

    private lateinit var constView: ConstraintLayout

    private lateinit var mediaProjectionManager: MediaProjectionManager
    var mediaProjection: MediaProjection? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        //TODO Start Notification with Switcher
        val notification = createNotification()

        startForeground(2, notification)

        //Filter UI
        val view = LinearLayout(this)
        constView = ConstraintLayout(this)
        constView.setBackgroundColor(Color.GRAY)
        constView.background.alpha = 80
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
        }
        val layoutParams = WindowManager.LayoutParams(
            MATCH_PARENT, MATCH_PARENT, flag, 280, PixelFormat.TRANSLUCENT
        )
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(constView, layoutParams)
        //TODO Add Blur LinearLayout with Coordinates for sansur cats


    }

    @SuppressLint("WrongConstant")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let{
            mediaProjection =
                mediaProjectionManager.getMediaProjection(-1, it.getParcelableExtra("data")!!)

            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val width: Int
            val height: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                width = windowManager.currentWindowMetrics.bounds.width()
                height = windowManager.currentWindowMetrics.bounds.height()
            } else {
                val metrics = DisplayMetrics()
                width = windowManager.defaultDisplay.width
                height = windowManager.defaultDisplay.height

            }

            val imageReader = ImageReader.newInstance(width,height,PixelFormat.RGBA_8888,2)
            val virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "ScreenCapture",
                width,
                height,
                getScreenDensity(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.surface,null,null
            )
            var counter = 0
            imageReader.setOnImageAvailableListener({imageRecode ->

                try {
                    val image = imageRecode.acquireLatestImage()
                    if (image != null) {
                        val fileout = FileOutputStream(File(filesDir, "deneme.jpg"))

                        val planes = image.planes
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        val bitmap = Bitmap.createBitmap(
                            width + rowPadding / pixelStride,
                            height,
                            Bitmap.Config.ARGB_8888
                        )
                        bitmap.copyPixelsFromBuffer(planes[0].buffer)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileout)
                        fileout.flush()
                        fileout.close()

                        bitmap.recycle()
                        counter++
                        Log.d("ImageRecorder",counter.toString())

                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }, Handler(Looper.getMainLooper()))

        }



        return Service.START_STICKY
    }

    fun getScreenDensity(): Int {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val conf = this.resources.configuration
            conf.densityDpi
        } else {
            val tempMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(tempMetrics)
            tempMetrics.densityDpi
        }
    }

    override fun onDestroy() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.removeView(constView)
        super.onDestroy()
    }

    private fun takeScreenShot() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val densityDpi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val conf = this.resources.configuration
            conf.densityDpi
        } else {
            val tempMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(tempMetrics)
            tempMetrics.densityDpi
        }

        //Save
        /*val file = File(filesDir,"example.png")
        val fileOutputStream = FileOutputStream(file)
        b.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()*/
    }


    private fun createNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(openAppIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        return NotificationCompat.Builder(this, "1")
            .setContentTitle("No More Cats")
            .setContentText("I don't wanna see cats")
            .setSmallIcon(R.drawable.ic_cat_whiskers)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}