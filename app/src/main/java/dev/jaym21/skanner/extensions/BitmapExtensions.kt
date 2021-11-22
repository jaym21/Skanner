package dev.jaym21.skanner.extensions

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF

internal fun Bitmap.scaledBitmap(width: Int, height: Int): Bitmap {
    val matrix = Matrix()
    matrix.setRectToRect(
        RectF(0f, 0f, this.width.toFloat(), this.height.toFloat()),
        RectF(0f, 0f, width.toFloat(), height.toFloat()),
        Matrix.ScaleToFit.CENTER
    )
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true)
}