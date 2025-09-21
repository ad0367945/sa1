package com.hythe.aitrading.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hythe.aitrading.R
import com.hythe.aitrading.vision.OcrEngine
import com.hythe.aitrading.logic.Analytics
import com.hythe.aitrading.util.ImageUtils
import kotlinx.coroutines.*

class CaptureService : Service() {
    private lateinit var projection: MediaProjection
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notif: Notification = NotificationCompat.Builder(this, "capture")
            .setSmallIcon(R.drawable.ic_notification_overlay)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Screen analysis running…")
            .setOngoing(true)
            .build()
        startForeground(2, notif)

        val data: Intent = intent?.getParcelableExtra("mp_data") ?: return START_NOT_STICKY
        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mpm.getMediaProjection(-1, data)

        val dm = resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val density = dm.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = projection.createVirtualDisplay(
            "ai-trading-cap", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader!!.surface, null, null
        )

        scope.launch { loop() }
        return START_STICKY
    }

    private suspend fun loop() {
        while (isActive) {
            val img = withContext(Dispatchers.IO) { imageReader?.acquireLatestImage() }
            if (img != null) {
                val bmp: Bitmap? = ImageUtils.fromImage(img)
                img.close()
                if (bmp != null) processFrame(bmp)
            }
            delay(350)
        }
    }

    private fun processFrame(frame: Bitmap) {
        val w = frame.width; val h = frame.height
        val priceCrop = Rect((0.05*w).toInt(), (0.10*h).toInt(), (0.95*w).toInt(), (0.22*h).toInt())
        val symbolCrop = Rect((0.05*w).toInt(), (0.04*h).toInt(), (0.60*w).toInt(), (0.10*h).toInt())

        val priceBmp = ImageUtils.crop(frame, priceCrop)
        val symBmp = ImageUtils.crop(frame, symbolCrop)

        OcrEngine.read(priceBmp) { priceText ->
            OcrEngine.read(symBmp) { symbolText ->
                val cleanPrice = com.hythe.aitrading.util.Parsing.toDouble(priceText)
                val symbol = com.hythe.aitrading.util.Parsing.toSymbol(symbolText)
                if (cleanPrice != null && symbol != null) {
                    Analytics.onTick(symbol, cleanPrice)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay?.release()
        imageReader?.close()
        projection.stop()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
