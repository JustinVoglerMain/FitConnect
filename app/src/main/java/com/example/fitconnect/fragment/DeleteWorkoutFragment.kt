package com.example.fitconnect.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitconnect.R
import com.example.fitconnect.databinding.FragmentDeleteWorkoutBinding

class DeleteWorkoutFragment : Fragment() {
    private var _binding: FragmentDeleteWorkoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_delete_workout, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}