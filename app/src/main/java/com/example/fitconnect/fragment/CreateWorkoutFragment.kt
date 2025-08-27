package com.example.fitconnect.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitconnect.R
import com.example.fitconnect.model.WeightedWorkout
import com.example.fitconnect.model.WorkoutModel
import com.example.fitconnect.ui.WorkoutViewModel

class CreateWorkoutFragment : Fragment() {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var durationEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var difficultyEditText: EditText
    private lateinit var daysEditText: EditText
    private lateinit var levelUpPointsEditText: EditText
    private lateinit var createWorkoutButton: Button

    // New views for adding weighted workouts
    private lateinit var weightedWorkoutNameEditText: EditText
    private lateinit var weightedWorkoutRepsEditText: EditText
    private lateinit var weightedWorkoutSetsEditText: EditText
    private lateinit var weightedWorkoutWeightEditText: EditText
    private lateinit var addWeightedWorkoutButton: Button

    private val weightedWorkouts = mutableListOf<WeightedWorkout>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(WorkoutViewModel::class.java)

        // Initialize Views
        nameEditText = view.findViewById(R.id.nameEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        durationEditText = view.findViewById(R.id.durationEditText)
        typeEditText = view.findViewById(R.id.typeEditText)
        difficultyEditText = view.findViewById(R.id.difficultyEditText)
        daysEditText = view.findViewById(R.id.daysEditText)
        levelUpPointsEditText = view.findViewById(R.id.levelUpPointsEditText)
        createWorkoutButton = view.findViewById(R.id.createWorkoutButton)

        // Initialize new views for weighted workouts
        weightedWorkoutNameEditText = view.findViewById(R.id.weightedWorkoutNameEditText)
        weightedWorkoutRepsEditText = view.findViewById(R.id.weightedWorkoutRepsEditText)
        weightedWorkoutSetsEditText = view.findViewById(R.id.weightedWorkoutSetsEditText)
        weightedWorkoutWeightEditText = view.findViewById(R.id.weightedWorkoutWeightEditText)
        addWeightedWorkoutButton = view.findViewById(R.id.addWeightedWorkoutButton)

        // Set button click listener for adding weighted workouts
        addWeightedWorkoutButton.setOnClickListener {
            addWeightedWorkout()
        }

        // Set button click listener for creating the workout
        createWorkoutButton.setOnClickListener {
            createWorkout()
        }
    }

    private fun addWeightedWorkout() {
        val name = weightedWorkoutNameEditText.text.toString()
        val reps = weightedWorkoutRepsEditText.text.toString().toIntOrNull() ?: 0
        val sets = weightedWorkoutSetsEditText.text.toString().toIntOrNull() ?: 0
        val weight = weightedWorkoutWeightEditText.text.toString().toDoubleOrNull() ?: 0.0

        if (name.isNotBlank() && reps > 0 && sets > 0 && weight > 0) {
            val newWeightedWorkout = WeightedWorkout(name, reps, sets, weight)
            weightedWorkouts.add(newWeightedWorkout)

            // Provide feedback to the user
            Toast.makeText(context, "Weighted Workout added!", Toast.LENGTH_SHORT).show()

            // Clear input fields
            weightedWorkoutNameEditText.text.clear()
            weightedWorkoutRepsEditText.text.clear()
            weightedWorkoutSetsEditText.text.clear()
            weightedWorkoutWeightEditText.text.clear()
        } else {
            Toast.makeText(context, "Please fill in all fields for the Weighted Workout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createWorkout() {
        val name = nameEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val duration = durationEditText.text.toString().toIntOrNull() ?: 0
        val type = typeEditText.text.toString()
        val difficulty = difficultyEditText.text.toString()
        val days = daysEditText.text.toString().toIntOrNull() ?: 0
        val levelUpPoints = levelUpPointsEditText.text.toString().toIntOrNull() ?: 0

        val newWorkout = WorkoutModel(
            name = name,
            description = description,
            duration = duration,
            type = type,
            difficulty = difficulty,
            days = days,
            levelUpPoints = levelUpPoints,
            weightedWorkouts = weightedWorkouts
        )

        viewModel.addWorkout(newWorkout)

        // Provide feedback to the user
        Toast.makeText(context, "Workout created successfully!", Toast.LENGTH_SHORT).show()

        // Navigate back or close the fragment
        requireActivity().supportFragmentManager.popBackStack()
    }
}

