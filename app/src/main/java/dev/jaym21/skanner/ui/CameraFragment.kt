package dev.jaym21.skanner.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentCameraBinding
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.FileUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CameraFragment : Fragment(){

    private var binding: FragmentCameraBinding? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var documentDirectory: String? = null
    private var photoFile: File? = null
    private var isFlashOn = false
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

        //getting new document directory for saving images
        documentDirectory = arguments?.getString("documentDirectory")

        binding?.btnCloseCamera?.setOnClickListener {
            val documentDirectoryFile = File(documentDirectory)
            val directoryAllFiles = documentDirectoryFile.listFiles()
            //deleting the directory whole if empty meaning new directory document is created
            if (directoryAllFiles.isEmpty()){
                documentDirectoryFile.delete()
            }
            navController.popBackStack()
        }

        if (checkCameraPermissions(requireContext(), arrayOf(Manifest.permission.CAMERA))) {
            initialize()
        } else {
            permissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }

        //handling back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val documentDirectoryFile = File(documentDirectory)
                val directoryAllFiles = documentDirectoryFile.listFiles()
                //deleting the directory whole if empty meaning new directory document is created
                if (directoryAllFiles.isEmpty()){
                    documentDirectoryFile.delete()
                }
                navController.popBackStack(R.id.allDocumentsFragment, false)
            }
        })
    }

    private fun initialize() {
        binding?.viewFinder?.post {
            setUpCamera()
        }

        binding?.ivTakePicture?.setOnClickListener {
            takePicture()
        }

        binding?.ivFlashlight?.setOnClickListener {
            if (camera?.cameraInfo!!.hasFlashUnit()) {
                if (isFlashOn) {
                    //changing the flash icon to off
                    binding?.ivFlashlight?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_flash_off
                        )
                    )
                    //turning off flash
                    camera?.cameraControl?.enableTorch(false)
                    isFlashOn = false
                } else {
                    //changing the flash icon to on
                    binding?.ivFlashlight?.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_flash_on
                        )
                    )
                    //turning off flash
                    camera?.cameraControl?.enableTorch(true)
                    isFlashOn = true
                }
            }else {
                Toast.makeText(requireContext(), "Flash not available from device", Toast.LENGTH_SHORT).show()
            }
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
        binding?.progressBar?.visibility = View.VISIBLE

        // Creating time-stamped output file to hold the image
        photoFile = File(
            documentDirectory,
            SimpleDateFormat(
                Constants.FILENAME, Locale.US
            ).format(System.currentTimeMillis()) + Constants.PHOTO_EXTENSION
        )

        if (photoFile != null) {

            // Creating output option object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile!!).build()

            // Set up image capture listener which is triggered when image is taken
            imageCapture?.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(requireContext()),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val savedUri = outputFileResults.savedUri
                        binding?.progressBar?.visibility = View.GONE
                        navigateToCropImage(photoFile!!)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        binding?.progressBar?.visibility = View.GONE
                        Log.e(TAG, "onError: Photo capture failed: ${exception.message}")
                        Toast.makeText(
                            requireContext(),
                            "Failed to take picture, try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val documentDirectoryFile = File(documentDirectory)
                        val directoryAllFiles = documentDirectoryFile.listFiles()
                        //deleting the directory whole if empty meaning new directory document is created
                        if (directoryAllFiles.isEmpty()){
                            documentDirectoryFile.delete()
                        }
                        navController.popBackStack()
                    }
                })
        } else {
            Toast.makeText(requireContext(), "Failed to save the image, try again!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    private fun navigateToCropImage(photoFile: File) {
        requireActivity().runOnUiThread {
            val bundle = bundleOf("documentDirectory" to documentDirectory, "originalImageFilePath" to photoFile.absolutePath)
            navController.navigate(
                R.id.action_cameraFragment_to_imageCropFragment,
                bundle
            )
        }
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