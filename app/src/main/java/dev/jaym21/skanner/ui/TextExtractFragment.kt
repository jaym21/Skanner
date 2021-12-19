package dev.jaym21.skanner.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentTextExtractBinding


class TextExtractFragment : Fragment() {

    private var binding: FragmentTextExtractBinding? = null
    private lateinit var navController: NavController
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

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
            setUpCamera()
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            bindCameraUseCases()
        })
    }

    private fun bindCameraUseCases() {

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