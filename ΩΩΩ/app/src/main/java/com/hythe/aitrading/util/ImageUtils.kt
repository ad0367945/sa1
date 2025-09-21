package com.hythe.aitrading.util

import android.graphics.Bitmap
import android.media.Image
import java.nio.ByteBuffer

object ImageUtils {
    fun fromImage(image: Image): Bitmap? {
        val plane = image.planes[0]
        val width = image.width
        val height = image.height
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        val buffer: ByteBuffer = plane.buffer
        buffer.rewind()
        bitmap.copyPixelsFromBuffer(buffer)

        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    fun crop(src: Bitmap, r: android.graphics.Rect): Bitmap {
        val left = r.left.coerceAtLeast(0)
        val top = r.top.coerceAtLeast(0)
        val width = (r.width()).coerceAtMost(src.width - left)
        val height = (r.height()).coerceAtMost(src.height - top)
        return Bitmap.createBitmap(src, left, top, width, height)
    }
}
