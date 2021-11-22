package dev.jaym21.skanner.ui.components

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import dev.jaym21.skanner.R
import dev.jaym21.skanner.extensions.yuvToRgba
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.ImageDetectionProperties
import dev.jaym21.skanner.utils.OpenCVUtils
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import java.io.File
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal class ScanSurfaceView: FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val TAG = "ScanSurfaceView"
    lateinit var lifecycleOwner: LifecycleOwner
    lateinit var listener: ScanSurfaceListener
    lateinit var originalImageFile: File
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isCapturing = false
    private lateinit var previewSize: Size
    private var viewFinder: PreviewView
    private var scanCanvasView: ScanCanvasView

    init {
        LayoutInflater.from(context).inflate(R.layout.scan_surface_view, this, true)
        viewFinder = findViewById(R.id.viewFinder)
        scanCanvasView = findViewById(R.id.scanCanvasView)
    }

    fun start() {
        viewFinder.post {
            viewFinder.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            previewSize = Size(viewFinder.width, viewFinder.height)
            startCamera()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            try {
                bindCameraUseCases()
            } catch (e: Exception) {
                Log.e(TAG, "Binding use cases failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases() {

        cameraProvider?.unbindAll()
        camera = null

        //building preview object
        preview = Preview.Builder()
            .setTargetResolution(previewSize)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
            .also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

        if (imageCapture != null && cameraProvider?.isBound(imageCapture!!) == true) {
            cameraProvider?.unbind(imageCapture)
        }

        imageCapture = null
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        val aspectRatio: Float = previewSize.width / previewSize.height.toFloat()
        val width = Constants.IMAGE_ANALYSIS_SCALE_WIDTH
        val height = (width / aspectRatio).roundToInt()

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(width, height))
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        imageAnalysis?.setAnalyzer(ContextCompat.getMainExecutor(context), { image ->
            try {
                val mat = image.yuvToRgba()
                val originalPreviewSize = mat.size()
                val largestQuad = OpenCVUtils.detectLargestQuadrilateral(mat)
                mat.release()
                if (largestQuad != null) {
                    drawLargestRect(largestQuad.contour, largestQuad.points, originalPreviewSize)
                } else {
                    scanCanvasView.clearShape()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Detecting largest quadrilateral failed", e)
                scanCanvasView.clearShape()
            }
            image.close()
        })
        camera = cameraProvider!!.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis, imageCapture)
    }

    private fun drawLargestRect(approx: MatOfPoint2f, points: Array<Point>, stdSize: org.opencv.core.Size) {
        val previewWidth = stdSize.height.toFloat()
        val previewHeight = stdSize.width.toFloat()

        val resultWidth = max(previewWidth - points[0].y.toFloat(), previewWidth - points[1].y.toFloat()) -
                min(previewWidth - points[2].y.toFloat(), previewWidth - points[3].y.toFloat())

        val resultHeight = max(points[1].x.toFloat(), points[2].x.toFloat()) - min(points[0].x.toFloat(), points[3].x.toFloat())

        val imgDetectionPropsObj = ImageDetectionProperties(previewWidth.toDouble(), previewHeight.toDouble(),
            points[0], points[1], points[2], points[3], resultWidth.toInt(), resultHeight.toInt())
        if (imgDetectionPropsObj.isNotValidImage(approx)) {
            scanCanvasView.clearShape()
//            cancelAutoCapture()
        } else {
//            if (!isAutoCaptureScheduled) {
//                scheduleAutoCapture()
//            }
            scanCanvasView.showShape(previewWidth, previewHeight, points)
        }
    }

     fun takePicture() {
        listener.scanSurfaceShowProgress()
        isCapturing = true

        val imageCapture = imageCapture ?: return
        val outputOptions = ImageCapture.OutputFileOptions.Builder(originalImageFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                listener.scanSurfaceHideProgress()
                cameraProvider?.unbind(imageAnalysis)
                scanCanvasView.clearShape()
                if (outputFileResults.savedUri != null)
                    listener.scanSurfacePictureTaken(outputFileResults.savedUri!!)
                postDelayed({ isCapturing = false }, Constants.TIME_POST_PICTURE)
            }

            override fun onError(exception: ImageCaptureException) {
                listener.scanSurfaceHideProgress()
                Log.e(TAG, "Photo capture failed", exception)
            }
        })
    }
}