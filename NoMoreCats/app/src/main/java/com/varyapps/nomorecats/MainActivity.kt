package com.varyapps.nomorecats

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.canDrawOverlays
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.varyapps.nomorecats.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var virtualDisplay: VirtualDisplay

    private var resulCode: Int? = null
    private var resultData: Intent? = null

    private lateinit var service: RecognitionService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        service = RecognitionService()




        binding.btnStartService.setOnClickListener {
            startScreenCapture()
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!canDrawOverlays(this)) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package: $packageName")
                    )
                    startActivityForResult(intent, 0)
                } else {
                    //Start Recognition Service
                    val serviceIntent = Intent(this, RecognitionService::class.java)
                    ContextCompat.startForegroundService(this, serviceIntent)
                }
            } else {
                //Start Recognition Service
                val serviceIntent = Intent(this, RecognitionService::class.java)
                ContextCompat.startForegroundService(this, serviceIntent)
            }*/


        }


    }

    fun startScreenCapture() {
        if (this::mediaProjection.isInitialized) {
            setUpVirtualDisplay()
        } else if(resulCode != null && resultData != null) {
            setUpMediaProjection()
            setUpVirtualDisplay()
        }else{
            //Başlamak için izin iste - Kayıt başlatması
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 1)
        }

    }

    fun setUpVirtualDisplay() {
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

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            getScreenDensity(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            binding.surface.holder.surface,null,null
        )

    }

    fun stopScreenCapture(){
        if (this::virtualDisplay.isInitialized) {
            virtualDisplay.release()
        }
    }

    fun getScreenDensity(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val conf = this.resources.configuration
            conf.densityDpi
        } else {
            val tempMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(tempMetrics)
            tempMetrics.densityDpi
        }
    }

    fun setUpMediaProjection(){
        resultData?.let {
            mediaProjection =
                resulCode?.let { it1 -> mediaProjectionManager.getMediaProjection(it1, it) }!!
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1){
            resultData = data
            resulCode = resultCode
            val intent = Intent(this,service::class.java)
            intent.putExtra("data",resultData)
            ContextCompat.startForegroundService(this,intent)
            //setUpVirtualDisplay()

        }
    }
}