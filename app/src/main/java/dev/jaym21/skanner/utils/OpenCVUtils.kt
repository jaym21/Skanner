package dev.jaym21.skanner.utils

import android.graphics.Bitmap
import android.graphics.PointF
import dev.jaym21.skanner.extensions.scaleRectangle
import dev.jaym21.skanner.extensions.toMat
import dev.jaym21.skanner.model.Quadrilateral
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class OpenCVUtils {
    companion object {

        fun getContourEdgePoints(bitmap: Bitmap): List<PointF> {
            var point2f = getPoint(bitmap)
            if (point2f == null)
                point2f = MatOfPoint2f()

            val points: List<Point> = point2f.toArray().toList()
            val result: MutableList<PointF> = ArrayList()
            for (i in points.indices) {
                result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
            }
            return result
        }

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

        private fun findQuadrilateral(mContourList: List<MatOfPoint>): Quadrilateral? {
            for (c in mContourList) {
                val c2f = MatOfPoint2f(*c.toArray())
                val peri = Imgproc.arcLength(c2f, true)
                val approx = MatOfPoint2f()
                Imgproc.approxPolyDP(c2f, approx, Constants.EPSILON_CONSTANT * peri, true)
                val points = approx.toArray()
                // select biggest 4 angles polygon
                if (approx.rows() == Constants.ANGLES_NUMBER) {
                    val foundPoints: Array<Point> = sortPoints(points)
                    return Quadrilateral(approx, foundPoints)
                } else if(approx.rows() == 5) {
                    // if document has a bent corner
                    var shortestDistance = Int.MAX_VALUE.toDouble()
                    var shortestPoint1: Point? = null
                    var shortestPoint2: Point? = null

                    var diagonal = 0.toDouble()
                    var diagonalPoint1: Point? = null
                    var diagonalPoint2: Point? = null

                    for (i in 0 until 4) {
                        for (j in i + 1 until 5) {
                            val d = distance(points[i], points[j])
                            if (d < shortestDistance) {
                                shortestDistance = d
                                shortestPoint1 = points[i]
                                shortestPoint2 = points[j]
                            }
                            if(d > diagonal) {
                                diagonal = d
                                diagonalPoint1 = points[i]
                                diagonalPoint2 = points[j]
                            }
                        }
                    }

                    val trianglePointWithHypotenuse: Point? = points.toList().minus(arrayListOf(shortestPoint1, shortestPoint2, diagonalPoint1, diagonalPoint2))[0]

                    val newPoint = if(trianglePointWithHypotenuse!!.x > shortestPoint1!!.x && trianglePointWithHypotenuse.x > shortestPoint2!!.x &&
                        trianglePointWithHypotenuse.y > shortestPoint1.y && trianglePointWithHypotenuse.y > shortestPoint2.y) {
                        Point(min(shortestPoint1.x, shortestPoint2.x), min(shortestPoint1.y, shortestPoint2.y))
                    } else if(trianglePointWithHypotenuse.x < shortestPoint1.x && trianglePointWithHypotenuse.x < shortestPoint2!!.x &&
                        trianglePointWithHypotenuse.y > shortestPoint1.y && trianglePointWithHypotenuse.y > shortestPoint2.y) {
                        Point(max(shortestPoint1.x, shortestPoint2.x), min(shortestPoint1.y, shortestPoint2.y))
                    } else if(trianglePointWithHypotenuse.x < shortestPoint1.x && trianglePointWithHypotenuse.x < shortestPoint2!!.x &&
                        trianglePointWithHypotenuse.y < shortestPoint1.y && trianglePointWithHypotenuse.y < shortestPoint2.y) {
                        Point(max(shortestPoint1.x, shortestPoint2.x), max(shortestPoint1.y, shortestPoint2.y))
                    } else if(trianglePointWithHypotenuse.x > shortestPoint1.x && trianglePointWithHypotenuse.x > shortestPoint2!!.x &&
                        trianglePointWithHypotenuse.y < shortestPoint1.y && trianglePointWithHypotenuse.y < shortestPoint2.y) {
                        Point(min(shortestPoint1.x, shortestPoint2.x), max(shortestPoint1.y, shortestPoint2.y))
                    } else {
                        Point(0.0, 0.0)
                    }

                    val sortedPoints = sortPoints(arrayOf(trianglePointWithHypotenuse, diagonalPoint1!!, diagonalPoint2!!, newPoint))
                    val newApprox = MatOfPoint2f()
                    newApprox.fromArray(*sortedPoints)
                    return Quadrilateral(newApprox, sortedPoints)
                }
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

        private fun distance(p1: Point, p2: Point): Double {
            return sqrt((p1.x - p2.x).pow(2.0) + (p1.y - p2.y).pow(2.0))
        }

        private fun sortPoints(src: Array<Point>): Array<Point> {
            val srcPoints: ArrayList<Point> = ArrayList(src.toList())
            val result = arrayOf<Point?>(null, null, null, null)
            val sumComparator: Comparator<Point> = Comparator<Point> { lhs, rhs -> (lhs.y + lhs.x).compareTo(rhs.y + rhs.x) }
            val diffComparator: Comparator<Point> = Comparator<Point> { lhs, rhs -> (lhs.y - lhs.x).compareTo(rhs.y - rhs.x) }

            // top-left corner = minimal sum
            result[0] = Collections.min(srcPoints, sumComparator)
            // bottom-right corner = maximal sum
            result[2] = Collections.max(srcPoints, sumComparator)
            // top-right corner = minimal difference
            result[1] = Collections.min(srcPoints, diffComparator)
            // bottom-left corner = maximal difference
            result[3] = Collections.max(srcPoints, diffComparator)
            return result.map {
                it!!
            }.toTypedArray()
        }

        private fun getPoint(bitmap: Bitmap): MatOfPoint2f? {
            val src = bitmap.toMat()
            val ratio = Constants.DOWNSCALE_IMAGE_SIZE / max(src.width(), src.height())
            val downscaledSize = Size(src.width() * ratio, src.height() * ratio)
            val downscaled = Mat(downscaledSize, src.type())
            Imgproc.resize(src, downscaled, downscaledSize)
            val largestRectangle = detectLargestQuadrilateral(downscaled)

            return largestRectangle?.contour?.scaleRectangle(1f / ratio)
        }
    }
}