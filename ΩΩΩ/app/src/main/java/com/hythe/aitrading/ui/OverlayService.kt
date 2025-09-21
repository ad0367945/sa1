package com.hythe.aitrading.ui

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.hythe.aitrading.R
import com.hythe.aitrading.logic.Analytics
import com.hythe.aitrading.logic.Signal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var view: View? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.overlay_card, null)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 24; params.y = 120
        wm.addView(view, params)

        scope.launch { Analytics.signals.collect { update(it) } }
    }

    private fun update(sig: Signal) {
        view?.findViewById<TextView>(R.id.txtSymbol)?.text = sig.symbol
        view?.findViewById<TextView>(R.id.txtAction)?.text = sig.action
        view?.findViewById<TextView>(R.id.txtReason)?.text = sig.reason
        view?.findViewById<TextView>(R.id.txtConf)?.text = ("%.0f%%".format(sig.confidence * 100))
    }

    override fun onDestroy() { super.onDestroy(); view?.let { wm.removeView(it) } }
    override fun onBind(intent: Intent?): IBinder? = null
}
