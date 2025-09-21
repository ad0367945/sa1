package com.hythe.aitrading.ui

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hythe.aitrading.R
import com.hythe.aitrading.service.CaptureService

class MainActivity : AppCompatActivity() {
    private val REQ_MEDIA_PROJECTION = 4001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotifChannel()

        findViewById<Button>(R.id.btnStart).setOnClickListener { startFlow() }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopService(Intent(this, CaptureService::class.java))
            stopService(Intent(this, OverlayService::class.java))
        }
    }

    private fun startFlow() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
            startActivity(intent)
            return
        }
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQ_MEDIA_PROJECTION)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK && data != null) {
            startService(Intent(this, OverlayService::class.java))
            val svc = Intent(this, CaptureService::class.java)
            svc.putExtra("mp_data", data)
            startForegroundService(svc)
        }
    }

    private fun createNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("capture", getString(R.string.notif_channel_name), NotificationManager.IMPORTANCE_LOW)
            ch.description = getString(R.string.notif_channel_desc)
            ch.enableLights(false); ch.enableVibration(false); ch.lightColor = Color.BLUE
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(ch)
        }
    }
}
