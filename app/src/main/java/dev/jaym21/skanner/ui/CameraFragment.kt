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
import dev.jaym21.skanner.ui.components.ScanSurfaceListener
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.ImageDetectionProperties
import dev.jaym21.skanner.utils.OpenCVUtils
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt


class CameraFragment : Fragment(), ScanSurfaceListener{

    private var binding: FragmentCameraBinding? = null
    private lateinit var navController: NavController

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

        //setting lifecycleOwner for scanSurfaceView
        binding?.scanSurfaceView?.lifecycleOwner = this
        binding?.scanSurfaceView?.listener = this

        binding?.ivCloseCamera?.setOnClickListener {
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun scanSurfacePictureTaken() {

    }

    override fun scanSurfaceShowProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    override fun scanSurfaceHideProgress() {
        binding?.progressBar?.visibility = View.GONE
    }
}