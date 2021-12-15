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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.jaym21.skanner.R
import dev.jaym21.skanner.adapters.IImagesRVAdapter
import dev.jaym21.skanner.adapters.ImagesRVAdapter
import dev.jaym21.skanner.databinding.FragmentOpenDocumentBinding
import dev.jaym21.skanner.models.Document
import dev.jaym21.skanner.utils.ImageViewerActivity
import java.io.File
import java.io.FileOutputStream

class OpenDocumentFragment : Fragment(), IImagesRVAdapter {

    private var binding: FragmentOpenDocumentBinding? = null
    private lateinit var navController: NavController
    private lateinit var viewModel: DocumentViewModel
    private var openDocumentPath: String? = null
    private var openDocument: Document? = null
    private var documentPath: String? = null
    private var documentDirectory: File? = null
    private lateinit var imagesAdapter: ImagesRVAdapter
    private var allImages = arrayListOf<Bitmap>()
    private var allImagesPath = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOpenDocumentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //initializing viewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(DocumentViewModel::class.java)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting document id to open from argument
        openDocumentPath = arguments?.getString("openDocumentPath")
        viewModel.allDocuments.observe(viewLifecycleOwner, Observer { documents ->
            documents.forEach {
                if (it.path == openDocumentPath) {
                    setDocumentToBeOpened(it)
                }
            }
        })

        binding?.ivClose?.setOnClickListener {
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }

        //handling back press
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navController.popBackStack(R.id.allDocumentsFragment, false)
            }
        })
    }

    private fun setDocumentToBeOpened(document: Document) {
        //setting document
        openDocument = document
        //getting the path of document
        documentPath = document.path
        //getting the file directory of document using path
        documentDirectory = File(documentPath)
        showDocument()
    }

    private fun showDocument() {

        if (openDocument != null) {

            //setting document name
            binding?.tvDocumentName?.text = openDocument!!.name

            //adding all the images in  directory to array for passing them to recycler view adapter
            allImages.clear()
            documentDirectory!!.listFiles()!!.forEach {
                if (it.toString().substring(108) == ".jpg") {
                    val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                    allImages.add(bitmap)
                    allImagesPath.add(it.absolutePath)
                }
            }

            //initializing adapter
            imagesAdapter = ImagesRVAdapter(allImages.toList(), this)

            setUpRecyclerView()

            binding?.ivEdit?.setOnClickListener {
                editAlertDialog()
            }

            binding?.ivMoreOptions?.setOnClickListener {
                //creating a popup menu to show more menu options
                val popup = PopupMenu(requireContext(), binding?.ivMoreOptions)
                //inflating popup menu with layout
                popup.inflate(R.menu.documents_option_menu)

                //adding menu item click listener
                popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when(item.itemId) {
                        R.id.option_delete -> {
                            deleteAlertDialog(openDocument!!)
                            return@OnMenuItemClickListener true
                        }
                        R.id.option_share_pdf -> {
                            convertDocumentToPDFAndShare(openDocument!!)
                            return@OnMenuItemClickListener true
                        }
                        else ->
                            return@OnMenuItemClickListener false
                    }
                })
                //displaying the popup menu
                popup.show()
            }

            binding?.fabAddMore?.setOnClickListener {
                val bundle = bundleOf("documentDirectory" to openDocument?.path)
                navController.navigate(R.id.action_openDocumentFragment_to_cameraFragment, bundle)
            }
        } else {
            Toast.makeText(requireContext(), "No document found in memory", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editAlertDialog() {
        val alertBuilder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.edit_dialog_layout, null)
        val nameEditText: EditText = dialogLayout.findViewById(R.id.etNewName)
        val btnReject: ImageView = dialogLayout.findViewById(R.id.ivRejectName)
        val btnAccept: ImageView = dialogLayout.findViewById(R.id.ivAcceptName)

        alertBuilder.setView(dialogLayout)
        val editNameDialog = alertBuilder.create()
        editNameDialog.setCanceledOnTouchOutside(false)

        btnAccept.setOnClickListener {
            val newName = nameEditText.text.toString()
            if (openDocument != null) {
                val newDocument = Document(openDocument!!.id, newName, openDocument!!.path, openDocument!!.pdfPath, openDocument!!.pageCount)
                viewModel.updateDocument(newDocument)
                updateName(newName)
                editNameDialog.dismiss()
            }
        }

        btnReject.setOnClickListener {
            editNameDialog.dismiss()
        }

        editNameDialog.show()
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
            navController.popBackStack(R.id.allDocumentsFragment, false)
        }
        btnReject.setOnClickListener {
            deleteDialog.dismiss()
        }
        deleteDialog.show()
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

    private fun getUriFromFile(pdfFilePath: String): Uri {
        val pdfFile = File(pdfFilePath)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(), "dev.jaym21.skanner" + ".provider", pdfFile)
        } else {
            return Uri.fromFile(pdfFile)
        }
    }

    private fun updateName(newName: String) {
        binding?.tvDocumentName?.text = newName
    }

    private fun setUpRecyclerView() {
        binding?.rvImages?.apply {
            adapter = imagesAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onImageClick(position: Int) {
        val clickedImage = allImagesPath[position]
        val intent = Intent(requireContext(), ImageViewerActivity::class.java)
        intent.putExtra("clickedImage", clickedImage)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), binding?.clOpenDocumentRoot!!, "image")
        startActivity(intent, options.toBundle())
    }
}