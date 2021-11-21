package dev.jaym21.skanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.skanner.databinding.FragmentCameraBinding
import dev.jaym21.skanner.extensions.yuvToRgba
import dev.jaym21.skanner.utils.Constants
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class CameraFragment : Fragment() {

    private var binding: FragmentCameraBinding? = null
    private var displayId: Int = -1
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var cameraExecutor: ExecutorService
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var outputDirectory: File
    private lateinit var navController: NavController
    private lateinit var previewSize: Size

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting size of previewView
        previewSize = Size(binding?.viewFinder!!.width, binding?.viewFinder!!.height)

        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Set up the camera and its use cases
                    binding?.viewFinder?.post {
                        // Keep track of the display in which this view is attached
                        displayId = binding?.viewFinder?.display!!.displayId
                        setUpCamera()
                        binding?.ivTakePicture?.setOnClickListener {
                            takePicture()
                        }
                    }
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE)
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), Constants.CAMERA_REQUEST_CODE)
        }

    }


    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            //to build and bind camera use cases
            bindCameraUseCases()
        }, cameraExecutor)
    }

    private fun bindCameraUseCases() {
        val screenAspectRatio = aspectRatio(binding?.viewFinder!!.width, binding?.viewFinder!!.height)

        val rotation = binding?.viewFinder?.display!!.rotation

        // CameraProvider
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        //building preview object
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()

        val aspectRatio = previewSize.width / previewSize.height.toFloat()
        val width = Constants.IMAGE_ANALYSIS_SCALE_WIDTH
        val height = (width / aspectRatio).roundToInt()

        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(width, height))
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        imageAnalysis?.setAnalyzer(cameraExecutor, { image ->
            val mat = image.yuvToRgba()
            val originalPreviewSize = mat.size()
            val largestQuad = native
        })

        //unbinding the use-cases before again binding them
        cameraProvider.unbindAll()


    }

    private fun takePicture() {

    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - Constants.RATIO_4_3_VALUE) <= abs(previewRatio - Constants.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding?.viewFinder?.post {
                        // Keep track of the display in which this view is attached
                        displayId = binding?.viewFinder?.display!!.displayId
                        setUpCamera()
                        binding?.ivTakePicture?.setOnClickListener {
                            takePicture()
                        }
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Camera permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding?.viewFinder?.post {
                        // Keep track of the display in which this view is attached
                        displayId = binding?.viewFinder?.display!!.displayId
                        setUpCamera()
                        binding?.ivTakePicture?.setOnClickListener {
                            takePicture()
                        }
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Reading external storage permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding?.viewFinder?.post {
                        // Keep track of the display in which this view is attached
                        displayId = binding?.viewFinder?.display!!.displayId
                        setUpCamera()
                        binding?.ivTakePicture?.setOnClickListener {
                            takePicture()
                        }
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Writing external storage permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}