package dev.jaym21.skanner.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dev.jaym21.skanner.R
import dev.jaym21.skanner.databinding.FragmentAllDocumentsBinding

class AllDocumentsFragment : Fragment() {

    private var binding: FragmentAllDocumentsBinding? = null
    private lateinit var navController: NavController

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

        binding?.fabCamera?.setOnClickListener {
            navController.navigate(R.id.action_allDocumentsFragment_to_cameraFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}