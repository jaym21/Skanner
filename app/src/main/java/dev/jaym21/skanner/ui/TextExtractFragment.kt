package dev.jaym21.skanner.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentTextExtractBinding
import dev.jaym21.skanner.utils.Constants
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class TextExtractFragment : Fragment() {

    private var binding: FragmentTextExtractBinding? = null
    private lateinit var navController: NavController
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var displayId: Int = -1
    private lateinit var metrics: Rect
    private val detector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        lifecycle.addObserver(detector)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTextExtractBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing navController
        navController = Navigation.findNavController(view)

        binding?.ivClose?.setOnClickListener {
            navController.popBackStack()
        }

        if (checkCameraPermissions(requireContext(), arrayOf(Manifest.permission.CAMERA))) {
            initialize()
        } else {
            permissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun initialize() {

        binding?.ivCopyText?.setOnClickListener {
            if (!binding?.tvExtractedText?.text.isNullOrEmpty()) {
                copyExtractedTextToClipboard()
                Snackbar.make(binding?.root!!, "Text copied", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding?.root!!, "No extracted text found", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding?.viewFinder?.post {
            displayId = binding?.viewFinder?.display!!.displayId

            //getting display metrics
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metrics = requireActivity().windowManager.currentWindowMetrics.bounds
            }

            setUpCamera()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        //unbind the use-cases before rebinding them
        cameraProvider?.unbindAll()

        val screenAspectRatio = aspectRatio(binding?.viewFinder?.width!!, binding?.viewFinder?.height!!)

        val rotation = binding?.viewFinder?.display?.rotation

        //camera provider
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        //camera selector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        //preview
        preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation!!)
            .build()

        //analyzer
        imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(screenAspectRatio)
            .build()

        imageAnalyzer?.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), ImageAnalysis.Analyzer { imageProxy ->
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image  = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                recognizeText(image, imageProxy)
            }
        })

        //image capture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalyzer)

            preview?.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
        }catch (exc: Exception) {
            Log.d("TAG", "bindCameraUseCases: Failed to bind use cases")
        }

    }

    private fun recognizeText(image: InputImage, imageProxy: ImageProxy) {
        detector.process(image)
            .addOnSuccessListener { text ->
                binding?.tvExtractedText?.text = text.text
                imageProxy.close()
            }
            .addOnFailureListener { exception ->
                Log.d("TAGYOYO", "recognizeText: $exception")
                Toast.makeText(requireContext(), "Failed to recognize text", Toast.LENGTH_SHORT).show()
                imageProxy.close()
            }
    }

    private fun copyExtractedTextToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", binding?.tvExtractedText?.text)
        clipboard.setPrimaryClip(clip)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - Constants.RATIO_4_3_VALUE) <= abs(previewRatio - Constants.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    private fun checkCameraPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private val permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            initialize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}