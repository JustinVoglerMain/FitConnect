package com.example.fitconnect.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.adapter.WorkoutAdapter
import com.example.fitconnect.ui.WorkoutViewModel

class AddWorkoutFragment : Fragment() {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var adapter: WorkoutAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)

        // Initialize Views
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)

        // Initialize Adapter
        adapter = WorkoutAdapter(
            workouts = mutableListOf(),
            onDeleteClick = { /* No delete functionality needed */ },
            onEditClick = { /* No edit functionality needed */ },
            onAddClick = { workout ->
                // Handle add click
                viewModel.addUserWorkout(requireContext(), workout)
            },
            onCompleteClick = {}
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observe LiveData from ViewModel
        viewModel.workouts.observe(viewLifecycleOwner) { workouts ->
            adapter.updateData(workouts)
        }

        // Load workouts
        viewModel.getWorkouts()

        // Set up SearchView listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle query submission
                query?.let {
                    viewModel.searchWorkouts(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text change
                newText?.let {
                    viewModel.searchWorkouts(it)
                }
                return false
            }
        })
    }
}



