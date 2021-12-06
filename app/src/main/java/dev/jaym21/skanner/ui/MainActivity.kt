package dev.jaym21.skanner.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import dev.jaym21.skanner.databinding.ActivityMainBinding
import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if (OpenCVLoader.initDebug()) {
            Log.d("TAGYOYO", "OpenCV loaded")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}