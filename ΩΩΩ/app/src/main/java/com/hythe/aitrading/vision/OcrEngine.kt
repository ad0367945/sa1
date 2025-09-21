package com.hythe.aitrading.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object OcrEngine {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun read(bitmap: Bitmap, onResult: (String) -> Unit) {
        val img = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(img)
            .addOnSuccessListener { res -> onResult(res.text.replace("\n", " ").trim()) }
            .addOnFailureListener { onResult("") }
    }
}
