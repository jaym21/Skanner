package dev.jaym21.skanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.skanner.databinding.FragmentCameraBinding
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment(){

    private var binding: FragmentCameraBinding? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var outputDirectory: File
    private lateinit var navController: NavController
    private val TAG = "CameraFragment"

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

        //getting output directory for saving images
        outputDirectory = FileUtils.getOutputDirectory(requireActivity())

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    initialize()
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

    private fun initialize() {
        binding?.viewFinder?.post {
            setUpCamera()
        }

        binding?.ivTakePicture?.setOnClickListener {
            takePicture()
        }

        binding?.btnCloseCamera?.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun bindCameraUseCases() {

        val previewSize =  Size(binding?.viewFinder!!.width, binding?.viewFinder!!.height)

        // Must unbind the use-cases before rebinding them
        cameraProvider?.unbindAll()

        // Preview
        preview = Preview.Builder()
            .setTargetResolution(previewSize)
            .setTargetRotation(Surface.ROTATION_0)
            .build()
            .also {
                it.setSurfaceProvider(binding?.viewFinder!!.surfaceProvider)
            }

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(Surface.ROTATION_0)
            .build()

        try {
            camera = cameraProvider!!.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding?.viewFinder!!.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun takePicture() {
        // Creating time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                Constants.FILENAME, Locale.US
            ).format(System.currentTimeMillis()) + Constants.PHOTO_EXTENSION
        )

        // Creating output option object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener which is triggered when image is taken
        imageCapture?.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri
                navigateToCropImage(savedUri!!, photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "onError: Photo capture failed: ${exception.message}")
            }
        })
    }

    private fun navigateToCropImage(savedUri: Uri, photoFile: File) {
        requireActivity().runOnUiThread {
            val bundle = bundleOf("savedUri" to  savedUri.toString(), "originalImageFile" to photoFile)
            navController.navigate(dev.jaym21.skanner.R.id.action_cameraFragment_to_imageCropFragment, bundle)
        }
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
                    initialize()
                }else {
                    Snackbar.make(binding?.root!!, "Camera permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize()
                }else {
                    Snackbar.make(binding?.root!!, "Reading external storage permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize()
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