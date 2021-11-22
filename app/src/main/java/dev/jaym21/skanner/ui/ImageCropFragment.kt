package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentImageCropBinding
import dev.jaym21.skanner.extensions.scaledBitmap
import id.zelory.compressor.determineImageRotation
import java.io.File

class ImageCropFragment : Fragment() {

    private var binding: FragmentImageCropBinding? = null
    private lateinit var navController: NavController
    private lateinit var takenImageUri: Uri
    private lateinit var originalImageFile: File
    private var selectedImage: Bitmap? = null

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

//        selectedImage = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, takenImageUri)
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
    }

    private fun initImageCropping() {
        if (selectedImage != null  && selectedImage!!.width > 0 && selectedImage!!.height > 0) {
            val scaledBitmap = selectedImage!!.scaledBitmap(binding?.flImageCropHolder!!.width, binding?.flImageCropHolder!!.height)
            binding?.ivTakenImage?.setImageBitmap(scaledBitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}