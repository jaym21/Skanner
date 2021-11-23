package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentImageCropBinding
import dev.jaym21.skanner.extensions.scaledBitmap
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.OpenCVUtils
import id.zelory.compressor.determineImageRotation
import id.zelory.compressor.extension
import id.zelory.compressor.saveBitmap
import java.io.File

class ImageCropFragment : Fragment() {

    private var binding: FragmentImageCropBinding? = null
    private lateinit var navController: NavController
    private lateinit var takenImageUri: Uri
    private lateinit var originalImageFile: File
    private var selectedImage: Bitmap? = null
    private var transformedImage: Bitmap? = null
    private var transformedImageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentImageCropBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting uri of image taken from argument
        takenImageUri = arguments?.getString("savedUri")!!.toUri()

        originalImageFile = (arguments?.get("originalImageFile") as File?)!!

        val fileBitmap =  BitmapFactory.decodeFile(originalImageFile.absolutePath)

        if(fileBitmap != null) {
            selectedImage = determineImageRotation(originalImageFile, fileBitmap)
        } else {
            Toast.makeText(requireContext(), "Image capture failed, try again!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }

        binding?.flImageViewHolder?.post {
            initImageCropping()
        }

        binding?.ivClose?.setOnClickListener {
            navController.popBackStack()
        }

        binding?.ivAccept?.setOnClickListener {
            getCroppedImage()
        }
    }

    private fun initImageCropping() {
        if (selectedImage != null  && selectedImage!!.width > 0 && selectedImage!!.height > 0) {
            val scaledBitmap = selectedImage!!.scaledBitmap(binding?.flImageCropHolder!!.width, binding?.flImageCropHolder!!.height)
            binding?.ivTakenImage?.setImageBitmap(scaledBitmap)
            val tempBitmap = scaledBitmap
            val pointFs = getEdgePoints(tempBitmap)
            binding?.polygonView?.setPoints(pointFs)
            binding?.polygonView?.visibility = View.VISIBLE
            val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + 32, tempBitmap.height + 32)
            layoutParams.gravity = Gravity.CENTER
            binding?.polygonView?.layoutParams = layoutParams
        }
    }

    private fun getCroppedImage() {
        if(selectedImage != null) {
            try {
                val points: Map<Int, PointF> = binding?.polygonView!!.getPoints()
                val xRatio: Float = selectedImage!!.width.toFloat() / binding?.ivTakenImage!!.width
                val yRatio: Float = selectedImage!!.height.toFloat() / binding?.ivTakenImage!!.height
                val pointPadding = 20
                val x1: Float = (points.getValue(0).x + pointPadding) * xRatio
                val x2: Float = (points.getValue(1).x + pointPadding) * xRatio
                val x3: Float = (points.getValue(2).x + pointPadding) * xRatio
                val x4: Float = (points.getValue(3).x + pointPadding) * xRatio
                val y1: Float = (points.getValue(0).y + pointPadding) * yRatio
                val y2: Float = (points.getValue(1).y + pointPadding) * yRatio
                val y3: Float = (points.getValue(2).y + pointPadding) * yRatio
                val y4: Float = (points.getValue(3).y + pointPadding) * yRatio

                transformedImage = OpenCVUtils.getScannedBitmap(selectedImage!!, x1, y1, x2, y2, x3, y3, x4, y4)

                transformedImage?.let {
                    transformedImageFile = File(requireContext().filesDir, "${Constants.TRANSFORMED_IMAGE_NAME}.${Bitmap.CompressFormat.JPEG.extension()}")
                    saveBitmap(it, transformedImageFile!!, Bitmap.CompressFormat.JPEG, 100)
                }

                val bundle = bundleOf("transformedImageFile" to transformedImageFile)
                navController.navigate(R.id.action_imageCropFragment_to_imageProcessingFragment, bundle)

            } catch (e: java.lang.Exception) {
                Log.e("TAGYOYO", "Cropping image failed")
            }
        } else {
            Log.e("TAGYOYO", "Invalid Image")
        }
    }

    private fun getEdgePoints(bitmap: Bitmap): Map<Int, PointF> {
        val listOfPointF: List<PointF> = OpenCVUtils.getContourEdgePoints(bitmap)
        return binding?.polygonView!!.getOrderedValidEdgePoints(bitmap, listOfPointF)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}