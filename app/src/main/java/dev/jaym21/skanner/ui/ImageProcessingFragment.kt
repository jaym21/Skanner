package dev.jaym21.skanner.ui

import android.graphics.*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentImageProcessingBinding
import dev.jaym21.skanner.extensions.rotate
import dev.jaym21.skanner.extensions.toBitmap
import dev.jaym21.skanner.extensions.toMat
import dev.jaym21.skanner.models.Document
import dev.jaym21.skanner.utils.Constants
import id.zelory.compressor.saveBitmap
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.File


class ImageProcessingFragment : Fragment() {

    private var binding: FragmentImageProcessingBinding? = null
    private lateinit var viewModel: DocumentViewModel
    private lateinit var navController: NavController
    private var croppedImageFilePath: String? = null
    private var croppedImageFile: File? = null
    private var croppedImageBitmap: Bitmap? = null
    private var tempBitmap: Bitmap? = null
    private var documentDirectory: String? = null
    private var isGrayScaleApplied: Boolean = false
    private var isBWApplied: Boolean = false
    private var exportTemp = false

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

        //initializing viewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(DocumentViewModel::class.java)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting document directory from argument
        documentDirectory = arguments?.getString("documentDirectory")

        //getting file of cropped image from argument
        croppedImageFilePath = arguments?.getString("croppedImageFilePath")

        //getting file object from absolute path
        croppedImageFile = File(croppedImageFilePath)

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

        binding?.llBW?.setOnClickListener {
            applyBW()
        }

        binding?.ivClose?.setOnClickListener {
            val documentDirectoryFile = File(documentDirectory)
            val directoryAllFiles = documentDirectoryFile.listFiles()
            croppedImageFile!!.delete()
            //deleting the directory whole if empty meaning new directory document is created
            if (directoryAllFiles.size == 1){
                documentDirectoryFile.delete()
            }
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }

        binding?.ivAccept?.setOnClickListener {
            addImageToDirectoryUpdateDatabase()
        }

        //handling back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })
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
            val documentDirectoryFile = File(documentDirectory)
            val directoryAllFiles = documentDirectoryFile.listFiles()
            croppedImageFile!!.delete()
            //deleting the directory whole if empty meaning new directory document is created
            if (directoryAllFiles.size == 1){
                documentDirectoryFile.delete()
            }
            binding?.progressBar?.visibility = View.GONE
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }
    }

    private fun applyGrayScale() {
        binding?.progressBar?.visibility = View.VISIBLE
        if (tempBitmap != null) {
            if (!isGrayScaleApplied) {
                isBWApplied = true
                applyBW()

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
                exportTemp = true
            } else {
                binding?.ivGrayscaleIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.tvGrayScale?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.ivCroppedImage?.setImageBitmap(croppedImageBitmap)
                tempBitmap = croppedImageBitmap
                binding?.progressBar?.visibility = View.GONE
                exportTemp = false
            }
            isGrayScaleApplied = !isGrayScaleApplied
        } else {
            Toast.makeText(requireContext(), "No image found, try again!", Toast.LENGTH_SHORT)
                .show()
            val documentDirectoryFile = File(documentDirectory)
            val directoryAllFiles = documentDirectoryFile.listFiles()
            croppedImageFile!!.delete()
            //deleting the directory whole if empty meaning new directory document is created
            if (directoryAllFiles.size == 1){
                documentDirectoryFile.delete()
            }
            binding?.progressBar?.visibility = View.GONE
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }
    }

    private fun applyBW() {
        binding?.progressBar?.visibility = View.VISIBLE
        if (tempBitmap != null) {
            if (!isBWApplied) {
                isGrayScaleApplied = true
                applyGrayScale()
                binding?.ivBWIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_500))
                binding?.tvBW?.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_500))

                val bitmapBW = Bitmap.createBitmap(
                    tempBitmap!!.width,
                    tempBitmap!!.height,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmapBW)
                val paint = Paint()
                canvas.drawBitmap(tempBitmap!!, 0f, 0f, paint)

                val tmp = Mat(bitmapBW.width, bitmapBW.height, CvType.CV_8UC1)
                Utils.bitmapToMat(bitmapBW, tmp)

                val gray = Mat(bitmapBW.width, bitmapBW.height, CvType.CV_8UC1)
                Imgproc.cvtColor(tmp, gray, Imgproc.COLOR_BGR2GRAY)

                val destMat = Mat(gray.width(), gray.height(), gray.type())
                Imgproc.adaptiveThreshold(gray, destMat, 255.0,
                    Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4.0)

                tempBitmap = destMat.toBitmap()

                binding?.ivCroppedImage?.setImageBitmap(tempBitmap)
                binding?.progressBar?.visibility = View.GONE
                exportTemp = true
            }else {
                binding?.ivBWIcon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.tvBW?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                binding?.ivCroppedImage?.setImageBitmap(croppedImageBitmap)
                tempBitmap = croppedImageBitmap
                binding?.progressBar?.visibility = View.GONE
                exportTemp = false
            }
            isBWApplied = !isBWApplied
        } else {
            Toast.makeText(requireContext(), "No image found, try again!", Toast.LENGTH_SHORT)
                .show()
            val documentDirectoryFile = File(documentDirectory)
            val directoryAllFiles = documentDirectoryFile.listFiles()
            croppedImageFile!!.delete()
            //deleting the directory whole if empty meaning new directory document is created
            if (directoryAllFiles.size == 1){
                documentDirectoryFile.delete()
            }
            binding?.progressBar?.visibility = View.GONE
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }
    }

    private fun addImageToDirectoryUpdateDatabase() {
        if (exportTemp) {
            tempBitmap?.let {
                saveBitmap(it, croppedImageFile!!, Bitmap.CompressFormat.JPEG, 100)
            }
        } else {
            croppedImageBitmap?.let {
                saveBitmap(it, croppedImageFile!!, Bitmap.CompressFormat.JPEG, 100)
            }
        }
        viewModel.allDocuments.observe(viewLifecycleOwner, Observer { documents ->
            var isPresent = false
            documents.forEach {
                if (it.path == documentDirectory) {
                    isPresent = true
                    updateDocumentDirectory(it)
                }
            }
            if (!isPresent)
                addNewDocument()
        })
    }

    private fun updateDocumentDirectory(document: Document) {
        val updatedDocument = Document(document.id, document.name, document.path, document.pdfPath, document.pageCount + 1)
        viewModel.updateDocument(updatedDocument)
        val bundle = bundleOf("openDocumentPath" to updatedDocument.path)
        navController.navigate(R.id.action_imageProcessingFragment_to_openDocumentFragment, bundle)
    }

    private fun addNewDocument() {
        val documentName = documentDirectory?.substring(61)
        val pdfFile = File(documentDirectory, "$documentName${Constants.PDF_EXTENSION}")
        val newDocument = Document(0, documentName!!, documentDirectory!!, pdfFile.absolutePath,1)
        viewModel.addDocument(newDocument)
        val bundle = bundleOf("openDocumentPath" to newDocument.path)
        navController.navigate(R.id.action_imageProcessingFragment_to_openDocumentFragment, bundle)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}