package dev.jaym21.skanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentCameraBinding
import dev.jaym21.skanner.ui.components.ScanSurfaceListener
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.FileUtils
import java.io.File


class CameraFragment : Fragment(), ScanSurfaceListener{

    private var binding: FragmentCameraBinding? = null
    private lateinit var navController: NavController
    private lateinit var outputDirectory: File

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

        //getting output directory to save file in
        outputDirectory = FileUtils.getOutputDirectory(requireContext())

        //creating file to hold the image
        binding?.scanSurfaceView?.originalImageFile = FileUtils.createFile(outputDirectory, Constants.FILENAME, Constants.PHOTO_EXTENSION)

        //setting lifecycleOwner for scanSurfaceView
        binding?.scanSurfaceView?.lifecycleOwner = this
        binding?.scanSurfaceView?.listener = this

        binding?.btnCloseCamera?.setOnClickListener {
            navController.popBackStack()
        }

        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                    binding?.ivTakePicture?.setOnClickListener {
                        takePicture()
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

    private fun startCamera() {
        binding?.scanSurfaceView?.start()
    }

    private fun takePicture() {
        binding?.scanSurfaceView?.takePicture()
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
                    startCamera()
                    binding?.ivTakePicture?.setOnClickListener {
                        takePicture()
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Camera permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                    binding?.ivTakePicture?.setOnClickListener {
                        takePicture()
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Reading external storage permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
            Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                    binding?.ivTakePicture?.setOnClickListener {
                        takePicture()
                    }
                }else {
                    Snackbar.make(binding?.root!!, "Writing external storage permissions required to work", Snackbar.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            }
        }
    }

    override fun scanSurfacePictureTaken(savedUri: Uri, originalImageFile: File) {
        val bundle = bundleOf("savedUri" to  savedUri.toString(), "originalImageFile" to originalImageFile.absolutePath.toString())
        navController.navigate(R.id.action_cameraFragment_to_imageCropFragment, bundle)
    }

    override fun scanSurfaceShowProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun scanSurfaceHideProgress() {
        binding?.progressBar?.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}