package com.hythe.aitrading.util

object Parsing {
    fun toDouble(txt: String): Double? {
        val t = txt.replace(",", "").replace(" ", "")
        val cleaned = t.filter { it.isDigit() || it == '.' }
        return cleaned.toDoubleOrNull()
    }
    fun toSymbol(txt: String): String? {
        val norm = txt.uppercase().replace("/", "")
        val m = Regex("[A-Z]{2,10}(USDT|USD|BUSD)").find(norm)
        return m?.value
    }
}
