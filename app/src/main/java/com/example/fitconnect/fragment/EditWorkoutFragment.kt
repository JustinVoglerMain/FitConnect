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
import com.example.fitconnect.model.WorkoutModel
import com.example.fitconnect.ui.WorkoutViewModel

class EditWorkoutFragment : Fragment() {

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var workout: WorkoutModel

    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var durationEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var difficultyEditText: EditText
    private lateinit var daysEditText: EditText
    private lateinit var levelUpPointsEditText: EditText
    private lateinit var saveWorkoutButton: Button

    companion object {
        private const val ARG_ID = "workout_id"
        private const val ARG_NAME = "workout_name"
        private const val ARG_DESCRIPTION = "workout_description"
        private const val ARG_DURATION = "workout_duration"
        private const val ARG_TYPE = "workout_type"
        private const val ARG_DIFFICULTY = "workout_difficulty"
        private const val ARG_DAYS = "workout_days"
        private const val ARG_LEVEL_UP_POINTS = "workout_level_up_points"
        //private const val ARG_WEIGHTED_WORKOUTS = "weighted_workouts"

        fun newInstance(workout: WorkoutModel): EditWorkoutFragment {
            val fragment = EditWorkoutFragment()
            val args = Bundle()
            args.putString(ARG_ID, workout.id)
            args.putString(ARG_NAME, workout.name)
            args.putString(ARG_DESCRIPTION, workout.description)
            args.putInt(ARG_DURATION, workout.duration)
            args.putString(ARG_TYPE, workout.type)
            args.putString(ARG_DIFFICULTY, workout.difficulty)
            args.putInt(ARG_DAYS, workout.days)
            args.putInt(ARG_LEVEL_UP_POINTS, workout.levelUpPoints)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            workout = WorkoutModel(
                id = it.getString(ARG_ID)!!,
                name = it.getString(ARG_NAME)!!,
                description = it.getString(ARG_DESCRIPTION)!!,
                duration = it.getInt(ARG_DURATION),
                type = it.getString(ARG_TYPE)!!,
                difficulty = it.getString(ARG_DIFFICULTY)!!,
                days = it.getInt(ARG_DAYS),
                levelUpPoints = it.getInt(ARG_LEVEL_UP_POINTS)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_workout, container, false)
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
        saveWorkoutButton = view.findViewById(R.id.saveWorkoutButton)

        // Pre-fill the fields with current workout data
        nameEditText.setText(workout.name)
        descriptionEditText.setText(workout.description)
        durationEditText.setText(workout.duration.toString())
        typeEditText.setText(workout.type)
        difficultyEditText.setText(workout.difficulty)
        daysEditText.setText(workout.days.toString())
        levelUpPointsEditText.setText(workout.levelUpPoints.toString())

        // Handle save button click
        saveWorkoutButton.setOnClickListener {
            val updatedWorkout = workout.copy(
                name = nameEditText.text.toString(),
                description = descriptionEditText.text.toString(),
                duration = durationEditText.text.toString().toIntOrNull() ?: 0,
                type = typeEditText.text.toString(),
                difficulty = difficultyEditText.text.toString(),
                days = daysEditText.text.toString().toIntOrNull() ?: 0,
                levelUpPoints = levelUpPointsEditText.text.toString().toIntOrNull() ?: 0,
                //weightedWorkouts = workout.weightedWorkouts // Preserve the weighted workouts
            )

            viewModel.updateWorkout(updatedWorkout)

            Toast.makeText(context, "Workout updated successfully!", Toast.LENGTH_SHORT).show()

            // Navigate back or close the fragment
            parentFragmentManager.popBackStack()
        }
    }
}



