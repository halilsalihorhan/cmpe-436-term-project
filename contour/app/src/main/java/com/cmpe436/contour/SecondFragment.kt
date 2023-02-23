package com.cmpe436.contour

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.cmpe436.contour.databinding.FragmentSecondBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val viewModel: ContourViewModel by lazy {
        ViewModelProvider(this)[ContourViewModel::class.java]
    }
    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.contourView.viewModel = viewModel
        _binding!!.contourView.listenToViewModel()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding!!.buttonFirst.setOnClickListener {
            viewModel.deleteShape()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}