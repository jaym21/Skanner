package dev.jaym21.skanner.utils

import dev.jaym21.skanner.model.Quadrilateral
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class OpenCVUtils {
    companion object {

        fun detectLargestQuadrilateral(src: Mat): Quadrilateral? {
            val destination = Mat()
            Imgproc.blur(src, src, Size(Constants.BLURRING_KERNEL_SIZE, Constants.BLURRING_KERNEL_SIZE))

            Core.normalize(src, src, Constants.NORMALIZATION_MIN_VALUE, Constants.NORMALIZATION_MAX_VALUE, Core.NORM_MINMAX)

            Imgproc.threshold(src, src, Constants.TRUNCATE_THRESHOLD, Constants.NORMALIZATION_MAX_VALUE, Imgproc.THRESH_TRUNC)
            Core.normalize(src, src, Constants.NORMALIZATION_MIN_VALUE, Constants.NORMALIZATION_MAX_VALUE, Core.NORM_MINMAX)

            Imgproc.Canny(src, destination, Constants.CANNY_THRESHOLD_HIGH, Constants.CANNY_THRESHOLD_LOW)

            Imgproc.threshold(destination, destination, Constants.CUTOFF_THRESHOLD, Constants.NORMALIZATION_MAX_VALUE, Imgproc.THRESH_TOZERO)

            Imgproc.morphologyEx(
                destination, destination, Imgproc.MORPH_CLOSE,
                Mat(Size(Constants.CLOSE_KERNEL_SIZE, Constants.CLOSE_KERNEL_SIZE), CvType.CV_8UC1, Scalar(Constants.NORMALIZATION_MAX_VALUE)),
                Point(-1.0, -1.0), 1
            )

            val largestContour: List<MatOfPoint>? = findLargestContours(destination)
        }

        private fun findLargestContours(inputMat: Mat): List<MatOfPoint>? {
            val mHierarchy = Mat()
            val mContourList: List<MatOfPoint> = ArrayList()
        }
    }
}