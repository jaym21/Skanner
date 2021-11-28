package dev.jaym21.skanner.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentOpenDocumentBinding
import dev.jaym21.skanner.models.Document
import java.io.File

class OpenDocumentFragment : Fragment() {

    private var binding: FragmentOpenDocumentBinding? = null
    private lateinit var navController: NavController
    private lateinit var viewModel: DocumentViewModel
    private var openDocument: Document? = null
    private var documentPath: String? = null
    private var documentDirectory: File? = null
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
        viewModel = ViewModelProvider(this).get(DocumentViewModel::class.java)

        //initializing navController
        navController = Navigation.findNavController(view)

        //getting document to open from argument
        openDocument = arguments?.get("openDocument") as Document

        documentPath = openDocument?.path

        documentDirectory = File(documentPath)

        documentDirectory!!.listFiles()!!.forEach {
            val bitmap = BitmapFactory.decodeFile(it.absolutePath)
            allImages.add(bitmap)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}