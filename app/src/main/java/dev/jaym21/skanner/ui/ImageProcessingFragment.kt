package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentImageProcessingBinding
import java.io.File


class ImageProcessingFragment : Fragment() {

    private var binding: FragmentImageProcessingBinding? = null
    private lateinit var navController: NavController
    private var croppedImageFile: File? = null
    private var croppedImageBitmap: Bitmap? = null

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

        binding?.llRotate?.setOnClickListener {
            rotateImage()
        }
    }

    private fun rotateImage() {
        if (croppedImageBitmap!= null) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}