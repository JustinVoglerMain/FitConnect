package com.example.fitconnect.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.adapter.WorkoutAdapter
import com.example.fitconnect.databinding.FragmentWorkoutBinding
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.tools.DialogueTools
import com.example.fitconnect.ui.WorkoutViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutFragment : Fragment() {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var adapter: WorkoutAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var createWorkoutButton: Button
    private lateinit var addWorkoutButton: Button
    private lateinit var XPButton : Button
    private var _binding: FragmentWorkoutBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private lateinit var dialogueTools: DialogueTools
    private val db = FirebaseFirestore.getInstance()
    private val dbTools = DBTools()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        dialogueTools = DialogueTools(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize your views
        recyclerView = view.findViewById(R.id.recycler_view)
        createWorkoutButton = view.findViewById(R.id.createWorkoutButton)
        addWorkoutButton = view.findViewById(R.id.addWorkoutButton)
        XPButton = view.findViewById(R.id.XPButton)

        // Setup ViewModel and RecyclerView
        viewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)
        adapter = WorkoutAdapter(
            workouts = mutableListOf(),
            onDeleteClick = { workout ->
                viewModel.deleteWorkout(workout.id)
                Toast.makeText(context, "Workout deleted successfully!", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { workout ->
                // Navigate to EditWorkoutFragment, passing the selected workout
                val editWorkoutFragment = EditWorkoutFragment.newInstance(workout)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, editWorkoutFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onAddClick = { workout ->
                viewModel.addUserWorkout(requireContext(), workout)
                Toast.makeText(context, "Workout added to your list!", Toast.LENGTH_SHORT).show()
            },
            onCompleteClick = { workout ->
                viewModel.completeWorkout(requireContext(), workout.id)
                Toast.makeText(context, "Workout completed!", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observe LiveData from ViewModel
        viewModel.workouts.observe(viewLifecycleOwner) { workouts ->
            adapter.updateData(workouts)
        }

        // Load workouts
        viewModel.getWorkouts()

        // Set button click listener for creating new workout
        createWorkoutButton.setOnClickListener {
            // Navigate to CreateWorkoutFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateWorkoutFragment())
                .addToBackStack(null)
                .commit()
        }

        addWorkoutButton.setOnClickListener {
            // Navigate to AddWorkoutFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddWorkoutFragment())
                .addToBackStack(null)
                .commit()
        }

        XPButton.setOnClickListener {
            val userID = auth.currentUser?.uid ?:""
            dbTools.getUser(userID) { user ->
                if(user != null) {
                    val userXP = user.xp
                    dialogueTools.createOKDialogueMessage("User XP", "Current XP: $userXP")
                }
            }

        }
    }
}




