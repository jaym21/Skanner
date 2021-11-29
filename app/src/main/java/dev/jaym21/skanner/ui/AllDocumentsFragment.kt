package dev.jaym21.skanner.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.skanner.R
import dev.jaym21.skanner.adapters.DocumentsRVAdapter
import dev.jaym21.skanner.adapters.IDocumentAdapter
import dev.jaym21.skanner.databinding.FragmentAllDocumentsBinding
import dev.jaym21.skanner.models.Document
import dev.jaym21.skanner.utils.Constants
import dev.jaym21.skanner.utils.FileUtils
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

        //initializing navController
        navController = Navigation.findNavController(view)

        //initializing viewModel
        viewModel = ViewModelProvider(this).get(DocumentViewModel::class.java)

        setUpRecyclerView()

        viewModel.allDocuments.observe(viewLifecycleOwner, {
            documentsAdapter.submitList(it)
        })

        binding?.fabCamera?.setOnClickListener {
            //making new directory to add new images taken
            val newDocumentPath = FileUtils.mkdir(requireActivity(), "Skanner_${SimpleDateFormat(Constants.FILENAME, Locale.US).format(System.currentTimeMillis())}")
            Log.d("TAGYOYO", "NEW DOCUMENT DIRECTORY $newDocumentPath")
            val bundle = bundleOf("documentDirectory" to newDocumentPath.absolutePath)
            navController.navigate(R.id.action_allDocumentsFragment_to_cameraFragment, bundle)
        }
    }

    private fun setUpRecyclerView() {
        binding?.rvDocuments?.apply {
            adapter = documentsAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onDocumentClicked(document: Document) {

    }
}