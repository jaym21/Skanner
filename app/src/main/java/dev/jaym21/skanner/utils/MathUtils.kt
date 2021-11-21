package dev.jaym21.skanner.utils

import org.opencv.core.Point
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class MathUtils {
    companion object {

        fun getMaxCosine(maxCosine: Double, approxPoints: Array<Point>): Double {
            var newMaxCosine = maxCosine
            for (i in 2..4) {
                val cosine: Double = abs(angle(approxPoints[i % 4], approxPoints[i - 2], approxPoints[i - 1]))
                newMaxCosine = max(cosine, newMaxCosine)
            }
            return newMaxCosine
        }

        private fun angle(p1: Point, p2: Point, p0: Point): Double {
            val dx1 = p1.x - p0.x
            val dy1 = p1.y - p0.y
            val dx2 = p2.x - p0.x
            val dy2 = p2.y - p0.y
            return (dx1 * dx2 + dy1 * dy2) / sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10)
        }
    }
}