package dev.jaym21.skanner.ui

import android.graphics.*
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
    private var croppedImageFilePath: String? = null
    private var croppedImageBitmap: Bitmap? = null
    private var tempBitmap: Bitmap? = null
    private var documentDirectory: String? = null
    private var isGrayScaleApplied: Boolean = false
    private var toExport = 0

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

        //getting document directory from argument
        documentDirectory = arguments?.getString("documentDirectory")

        //getting file of cropped image from argument
        croppedImageFilePath = arguments?.getString("transformedImageFilePath")

        croppedImageBitmap = BitmapFactory.decodeFile(croppedImageFilePath)

        binding?.ivCroppedImage?.setImageBitmap(croppedImageBitmap)

        binding?.ivClose?.setOnClickListener {
            navController.popBackStack(R.id.allDocumentsFragment, false)
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
            croppedImageBitmap = croppedImageBitmap?.rotate(Constants.ANGLE_OF_ROTATION)
            binding?.ivCroppedImage?.setImageBitmap(tempBitmap)
            binding?.progressBar?.visibility = View.GONE
        } else {
            Toast.makeText(requireContext(), "No image found, try again!", Toast.LENGTH_SHORT)
                .show()
            navController.popBackStack(R.id.allDocumentsFragment, false)
            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun applyGrayScale() {
        binding?.progressBar?.visibility = View.VISIBLE
        if (tempBitmap != null) {
            if (!isGrayScaleApplied) {
                binding?.ivGrayscaleIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_500))
                binding?.tvGrayScale?.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))
                val bitmapMonochrome = Bitmap.createBitmap(tempBitmap!!.width, tempBitmap!!.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmapMonochrome)
                val colorMatrix = ColorMatrix()
                colorMatrix.setSaturation(0f)
                val paint = Paint()
                paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                canvas.drawBitmap(tempBitmap!!, 0f, 0f, paint)
                tempBitmap = bitmapMonochrome.copy(bitmapMonochrome.config, true)
                binding?.ivCroppedImage?.setImageBitmap(tempBitmap)
                binding?.progressBar?.visibility = View.GONE
                toExport = 1
            } else {
                binding?.ivGrayscaleIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.tvGrayScale?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.ivCroppedImage?.setImageBitmap(croppedImageBitmap)
                binding?.progressBar?.visibility = View.GONE
                toExport = 0
            }
            isGrayScaleApplied = !isGrayScaleApplied
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}