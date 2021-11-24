package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentImageProcessingBinding
import dev.jaym21.skanner.extensions.rotate
import dev.jaym21.skanner.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class ImageProcessingFragment : Fragment() {

    private var binding: FragmentImageProcessingBinding? = null
    private lateinit var navController: NavController
    private var croppedImageFile: File? = null
    private var croppedImageBitmap: Bitmap? = null
    private var tempBitmap: Bitmap? = null
    private var isGrayScaleApplied: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentImageProcessingBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting file of cropped image from argument
        croppedImageFile = arguments?.get("transformedImageFile") as File?

        croppedImageBitmap = BitmapFactory.decodeFile(croppedImageFile!!.absolutePath)

        binding?.ivCroppedImage?.setImageBitmap(croppedImageBitmap)

        binding?.ivClose?.setOnClickListener {
            navController.popBackStack(R.id.allDocumentsFragment, true)
        }

        tempBitmap = croppedImageBitmap

        binding?.llRotate?.setOnClickListener {
            rotateImage()
        }

        binding?.llGrayScale?.setOnClickListener {
            applyGrayScale()
        }
    }

    private fun rotateImage() {
        binding?.progressBar?.visibility = View.VISIBLE
        if (tempBitmap != null) {
            tempBitmap = tempBitmap?.rotate(Constants.ANGLE_OF_ROTATION)
            binding?.ivCroppedImage?.setImageBitmap(tempBitmap)
            binding?.progressBar?.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "No image found, try again!", Toast.LENGTH_SHORT)
                .show()
            navController.popBackStack(R.id.allDocumentsFragment, true)
            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun applyGrayScale() {
        binding?.progressBar?.visibility = View.VISIBLE
        if (tempBitmap != null) {
            if (!isGrayScaleApplied) {
                binding?.ivGrayscaleIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_500))
                binding?.tvGrayScale?.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}