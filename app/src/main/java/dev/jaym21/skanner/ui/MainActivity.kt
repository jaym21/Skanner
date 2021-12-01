package dev.jaym21.skanner.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.ActivityMainBinding
import dev.jaym21.skanner.utils.Constants
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private var navController: NavController? = null
    private var navGraph: NavGraph? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (OpenCVLoader.initDebug()) {
            Log.d("TAGYOYO", "OpenCV loaded")
        }

        navController = findNavController(R.id.nav_host_fragment)
        val graphInflater = navController!!.navInflater
        navGraph = graphInflater.inflate(R.navigation.nav_graph)


        updateOrRequestPermissions()
    }

    private fun updateOrRequestPermissions() {
        val isReadPermissionAvailable = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val isWritePermissionAvailable = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSDK29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = isReadPermissionAvailable
        writePermissionGranted = isWritePermissionAvailable || minSDK29


        if (!writePermissionGranted) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
        }

        if (!readPermissionGranted) {
            ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    navGraph!!.startDestination = R.id.permissionNotGrantedFragment
                    navController!!.graph = navGraph as NavGraph
                }else {
                    navGraph!!.startDestination = R.id.allDocumentsFragment
                    navController!!.graph = navGraph as NavGraph
                }
            }
            Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    navGraph!!.startDestination = R.id.permissionNotGrantedFragment
                    navController!!.graph = navGraph as NavGraph
                }else {
                    navGraph!!.startDestination = R.id.allDocumentsFragment
                    navController!!.graph = navGraph as NavGraph
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}