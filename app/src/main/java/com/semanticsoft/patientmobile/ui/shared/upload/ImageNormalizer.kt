package com.semanticsoft.patientmobile.ui.shared.upload

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageNormalizer {
    private const val MAX_DIMENSION = 2000
    private const val JPEG_QUALITY = 90

    fun normalize(source: File): File? {
        val normalizedName = buildNormalizedName(source.name)
        val dest = File(source.parentFile ?: return null, normalizedName)
        if (dest.exists()) dest.delete()

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(source.absolutePath, options)
        val srcWidth = options.outWidth
        val srcHeight = options.outHeight
        if (srcWidth <= 0 || srcHeight <= 0) return null

        val scale = minOf(1f, MAX_DIMENSION.toFloat() / maxOf(srcWidth, srcHeight).toFloat())
        if (scale >= 1f) return null

        options.apply {
            inJustDecodeBounds = false
            inSampleSize = (1f / scale).toInt().coerceIn(1, 8)
        }
        val loaded = BitmapFactory.decodeFile(source.absolutePath, options)
            ?: return null
        val targetW = maxOf(1, (srcWidth * scale / options.inSampleSize).toInt())
        val targetH = maxOf(1, (srcHeight * scale / options.inSampleSize).toInt())
        val resized = Bitmap.createScaledBitmap(loaded, targetW, targetH, true)
        loaded.recycle()

        FileOutputStream(dest).use { out ->
            if (!resized.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) {
                resized.recycle()
                return null
            }
        }
        resized.recycle()
        return dest
    }

    fun shouldNormalize(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext == "jpg" || ext == "jpeg" || ext == "png"
    }

    private fun buildNormalizedName(originalName: String): String {
        val dotIndex = originalName.lastIndexOf('.')
        val base = if (dotIndex > 0) originalName.substring(0, dotIndex) else originalName
        return "${base}-normalizata.jpg"
    }
}
