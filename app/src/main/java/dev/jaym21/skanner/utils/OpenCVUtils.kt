package dev.jaym21.skanner.utils

import dev.jaym21.skanner.model.Quadrilateral
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.min

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
            if (largestContour != null){
                return findQuadrilateral(largestContour)
            }
            return null
        }

        private fun findLargestContours(inputMat: Mat): List<MatOfPoint>? {
            val mHierarchy = Mat()
            val mContourList: List<MatOfPoint> = ArrayList()

            //finding contours as we are sorting by area, we can use RETR_LIST - faster than RETR_EXTERNAL
            Imgproc.findContours(inputMat, mContourList, mHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

            // Convert the contours to their Convex Hulls i.e. removes minor nuances in the contour
            val mHullList: MutableList<MatOfPoint> = ArrayList()
            val tempHullIndices = MatOfInt()
            for (i in mContourList.indices) {
                Imgproc.convexHull(mContourList[i], tempHullIndices)
                mHullList.add(hull2Points(tempHullIndices, mContourList[i]))
            }
            // Release mContourList as its job is done
            for (c in mContourList) {
                c.release()
            }
            tempHullIndices.release()
            mHierarchy.release()
            if (mHullList.size != 0) {
                mHullList.sortWith { lhs, rhs ->
                    Imgproc.contourArea(rhs).compareTo(Imgproc.contourArea(lhs))
                }
                return mHullList.subList(0, min(mHullList.size, Constants.FIRST_MAX_CONTOURS))
            }
            return null
        }

        private fun hull2Points(hull: MatOfInt, contour: MatOfPoint): MatOfPoint {
            val indexes = hull.toList()
            val points: MutableList<Point> = ArrayList()
            val ctrList = contour.toList()
            for (index in indexes) {
                points.add(ctrList[index])
            }
            val point = MatOfPoint()
            point.fromList(points)
            return point
        }
    }
}