package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import dev.jaym21.skanner.R
import dev.jaym21.skanner.adapters.ImagesRVAdapter
import dev.jaym21.skanner.databinding.FragmentOpenDocumentBinding
import dev.jaym21.skanner.models.Document
import java.io.File

class OpenDocumentFragment : Fragment() {

    private var binding: FragmentOpenDocumentBinding? = null
    private lateinit var navController: NavController
    private lateinit var viewModel: DocumentViewModel
    private var openDocumentPath: String? = null
    private var openDocument: Document? = null
    private var documentPath: String? = null
    private var documentDirectory: File? = null
    private lateinit var imagesAdapter: ImagesRVAdapter
    private var allImages = arrayListOf<Bitmap>()

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
                val bitmap = BitmapFactory.decodeFile(it.absolutePath)
                allImages.add(bitmap)
            }

            //initializing adapter
            imagesAdapter = ImagesRVAdapter(allImages.toList())

            setUpRecyclerView()

            binding?.ivEdit?.setOnClickListener {
                editAlertDialog()
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
}