package com.hythe.aitrading.logic

object Indicators {
    fun ema(values: List<Double>, period: Int): Double? {
        if (values.size < period) return null
        val k = 2.0 / (period + 1)
        var ema = values.take(period).average()
        for (v in values.drop(period)) ema = v * k + ema * (1 - k)
        return ema
    }

    fun rsi(closes: List<Double>, period: Int = 14): Double? {
        if (closes.size <= period) return null
        var gains = 0.0; var losses = 0.0
        for (i in 1..period) {
            val diff = closes[i] - closes[i-1]
            if (diff >= 0) gains += diff else losses -= diff
        }
        var avgGain = gains / period
        var avgLoss = losses / period
        for (i in period+1 until closes.size) {
            val diff = closes[i] - closes[i-1]
            val gain = if (diff > 0) diff else 0.0
            val loss = if (diff < 0) -diff else 0.0
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
        }
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return 100 - (100 / (1 + rs))
    }
}
