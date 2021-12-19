package dev.jaym21.skanner.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentTextExtractBinding
import dev.jaym21.skanner.utils.Constants
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class TextExtractFragment : Fragment() {

    private var binding: FragmentTextExtractBinding? = null
    private lateinit var navController: NavController
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var displayId: Int = -1
    private lateinit var metrics: Rect

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

        if (checkCameraPermissions(requireContext(), arrayOf(Manifest.permission.CAMERA))) {
            initialize()
        } else {
            permissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun initialize() {
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

    private fun bindCameraUseCases() {
        val screenAspectRatio = aspectRatio(binding?.viewFinder?.width!!, binding?.viewFinder?.height!!)

    }

    private fun checkCameraPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - Constants.RATIO_4_3_VALUE) <= abs(previewRatio - Constants.RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
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