package com.hythe.aitrading.net

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor

@Serializable data class AiReq(val model: String, val messages: List<Msg>, val temperature: Double = 0.1)
@Serializable data class Msg(val role: String, val content: String)
@Serializable data class Choice(val index: Int, val message: Msg)
@Serializable data class AiResp(val choices: List<Choice>)

@Serializable data class AiSignal(val symbol: String, val action: String, val confidence: Double, val reason: String)

object AiClient {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val client by lazy {
        val log = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        OkHttpClient.Builder().addInterceptor(log).build()
    }

    private const val API_KEY = "REPLACE_WITH_OPENAI_KEY"
    private const val API_URL = "https://api.openai.com/v1/chat/completions"

    private fun prompt(platformHints: List<String>, symbol: String, price: Double, closes: List<Double>): String =
        """
        You are a trading assistant. You receive:
        - platform_hints: $platformHints (e.g., Binance)
        - symbol: $symbol
        - last_price: $price
        - closes: last N close prices (chronological)
        Tasks:
        1) Infer platform context from hints. Assume Binance if unsure.
        2) Compute RSI(14) and EMA(50) from closes (if provided) and combine with last_price.
        3) Decide action in {BUY, SELL, WAIT}. Be conservative.
        4) Return ONLY strict JSON: {"symbol": "...", "action": "...", "confidence": 0.0-1.0, "reason": "..."}
        """

    fun analyze(platformHints: List<String>, symbol: String, price: Double, closes: List<Double>): com.hythe.aitrading.logic.Signal? {
        if (API_KEY.startsWith("REPLACE")) return null
        val sys = Msg("system", "You respond with compact JSON only. No extra text.")
        val closesJson = json.encodeToString(ListSerializer(Double.serializer()), closes)
        val user = Msg("user", prompt(platformHints, symbol, price, closes) + "\nInput closes JSON:" + closesJson)

        val req = AiReq(model = "gpt-4o-mini", messages = listOf(sys, user))
        val body = json.encodeToString(AiReq.serializer(), req).toRequestBody("application/json".toMediaType())
        val httpReq = Request.Builder().url(API_URL).addHeader("Authorization", "Bearer $API_KEY").post(body).build()

        client.newCall(httpReq).execute().use { resp ->
            val str = resp.body?.string() ?: return null
            val parsed = json.decodeFromString(AiResp.serializer(), str)
            val content = parsed.choices.firstOrNull()?.message?.content ?: return null
            return try {
                val sig = json.decodeFromString(AiSignal.serializer(), content)
                com.hythe.aitrading.logic.Signal(sig.symbol, sig.action, sig.confidence, sig.reason)
            } catch (_: Throwable) { null }
        }
    }
}
