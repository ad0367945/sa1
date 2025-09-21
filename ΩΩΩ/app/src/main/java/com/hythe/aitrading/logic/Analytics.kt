package com.hythe.aitrading.logic

import com.hythe.aitrading.net.AiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

private data class Series(val closes: MutableList<Double> = mutableListOf())

data class Signal(val symbol: String, val action: String, val confidence: Double, val reason: String)

object Analytics {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val seriesMap = ConcurrentHashMap<String, Series>()

    private val _signals = MutableSharedFlow<Signal>(extraBufferCapacity = 8)
    val signals = _signals.asSharedFlow()

    fun onTick(symbol: String, price: Double) {
        val s = seriesMap.getOrPut(symbol) { Series() }
        s.closes.add(price)
        if (s.closes.size > 600) s.closes.removeAt(0)

        val rsi = Indicators.rsi(s.closes)
        val ema50 = Indicators.ema(s.closes, 50)
        val last = s.closes.lastOrNull()

        if (rsi != null && ema50 != null && last != null) {
            val localAction = when {
                rsi < 30 && last > ema50 -> "BUY"
                rsi > 70 && last < ema50 -> "SELL"
                else -> "WAIT"
            }
            scope.launch { _signals.emit(Signal(symbol, localAction, 0.55, "Local: RSI=%.1f EMA50=%.2f".format(rsi, ema50))) }

            scope.launch {
                try {
                    val ai = AiClient.analyze(listOf("Binance"), symbol, last, s.closes.takeLast(120))
                    if (ai != null) _signals.emit(ai)
                } catch (_: Throwable) {}
            }
        }
    }
}
