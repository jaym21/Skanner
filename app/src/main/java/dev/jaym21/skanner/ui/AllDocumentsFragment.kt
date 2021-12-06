package dev.jaym21.skanner.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.skanner.BuildConfig
import dev.jaym21.skanner.R
import dev.jaym21.skanner.adapters.DocumentsRVAdapter
import dev.jaym21.skanner.adapters.IDocumentAdapter
import dev.jaym21.skanner.databinding.FragmentAllDocumentsBinding
import dev.jaym21.skanner.models.Document
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AllDocumentsFragment : Fragment(), IDocumentAdapter {

    private var binding: FragmentAllDocumentsBinding? = null
    private lateinit var navController: NavController
    private var documentsAdapter = DocumentsRVAdapter(this)
    private lateinit var viewModel: DocumentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllDocumentsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Add reorder feature

        //initializing navController
        navController = Navigation.findNavController(view)

        //initializing viewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(DocumentViewModel::class.java)

        setUpRecyclerView()

        viewModel.allDocuments.observe(viewLifecycleOwner, { documents ->
            documentsAdapter.submitList(documents)
            //checking if the recyclerview is empty
            if (documents.isNotEmpty())
                binding?.tvNoDocumentAdded?.visibility = View.GONE
            else
                binding?.tvNoDocumentAdded?.visibility = View.VISIBLE
        })

        binding?.ivCamera?.setOnClickListener {
            //making new directory to add new images taken
            val newDocumentPath = FileUtils.mkdir(requireActivity(), "Skanner_${SimpleDateFormat(Constants.FILENAME, Locale.US).format(System.currentTimeMillis())}")
            val bundle = bundleOf("documentDirectory" to newDocumentPath.absolutePath)
            navController.navigate(R.id.action_allDocumentsFragment_to_cameraFragment, bundle)
        }
    }

    private fun setUpRecyclerView() {
        binding?.rvDocuments?.apply {
            adapter = documentsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onDocumentClicked(document: Document) {
        val bundle = bundleOf("openDocumentPath" to document.path)
        navController.navigate(R.id.action_allDocumentsFragment_to_openDocumentFragment, bundle)
    }

    override fun onOptionDeleteClicked(document: Document) {
        deleteAlertDialog(document)
    }

    override fun onOptionSharePDFClicked(document: Document) {
        convertDocumentToPDFAndShare(document)
    }

    private fun convertDocumentToPDFAndShare(document: Document) {
        val documentDirectory = File(document.path)
        val images = arrayListOf<Bitmap>()
        documentDirectory.listFiles()!!.forEach {
            if (it.toString().substring(108) == ".jpg") {
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                images.add(bitmap)
            }
        }

        val pdfFile = File(document.pdfPath)
        if (pdfFile.exists()){
            Log.d("TAGYOYO", "PDF EXISTS")
        }else {
            val fOut = FileOutputStream(document.pdfPath)
            val pdfDocument = PdfDocument()
            var i = 0
            images.forEach {
                i++
                val pageInfo = PdfDocument.PageInfo.Builder(it.width, it.height, i).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page?.canvas
                val paint = Paint()
                canvas?.drawPaint(paint)
                paint.color = Color.WHITE
                canvas?.drawBitmap(it, 0f, 0f, null)
                pdfDocument.finishPage(page)
                it.recycle()
            }
            pdfDocument.writeTo(fOut)
            pdfDocument.close()
        }

        val sharePdfIntent =  Intent(Intent.ACTION_SEND)
        sharePdfIntent.putExtra(Intent.EXTRA_STREAM, getUriFromFile(document.pdfPath))
        sharePdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        sharePdfIntent.type = "application/pdf"
        startActivity(Intent.createChooser(sharePdfIntent, "Share document pdf"))
    }

    private fun deleteAlertDialog(document: Document) {
        val alertBuilder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.delete_dialog_layout, null)
        val confirmText: TextView = dialogLayout.findViewById(R.id.tvConfirmText)
        val btnReject: ImageView = dialogLayout.findViewById(R.id.ivRejectDelete)
        val btnAccept: ImageView = dialogLayout.findViewById(R.id.ivAcceptDelete)

        //adding document name in confirm text
        confirmText.text = "Are you sure you want to delete ${document.name}?"

        alertBuilder.setView(dialogLayout)
        val deleteDialog = alertBuilder.create()
        deleteDialog.setCanceledOnTouchOutside(false)

        btnAccept.setOnClickListener {
            //removing document from database
            viewModel.removeDocument(document)
            //deleting document directory from main directory
            val documentFile = File(document.path)
            documentFile.delete()
            deleteDialog.dismiss()
        }
        btnReject.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()
    }

    private fun getUriFromFile(pdfFilePath: String): Uri {
        val pdfFile = File(pdfFilePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".provider", pdfFile)
        } else {
        return Uri.fromFile(pdfFile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}