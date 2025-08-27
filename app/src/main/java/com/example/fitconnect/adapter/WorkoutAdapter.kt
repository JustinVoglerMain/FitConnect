package com.example.fitconnect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.WorkoutModel
import com.example.fitconnect.tools.WorkoutViewHolder

class WorkoutAdapter(
    private var workouts: MutableList<WorkoutModel>,
    private val onDeleteClick: (WorkoutModel) -> Unit,
    private val onEditClick: (WorkoutModel) -> Unit,
    private val onAddClick: (WorkoutModel) -> Unit,
    private val onCompleteClick: (WorkoutModel) -> Unit
) : RecyclerView.Adapter<WorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workout, parent, false)
        return WorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val workout = workouts[position]
        holder.nameTextView.text = workout.name
        holder.descriptionTextView.text = "Description: ${workout.description}"
        holder.durationTextView.text = "Duration: ${workout.duration} minutes"
        holder.typeTextView.text = "Type: ${workout.type}"
        holder.difficultyTextView.text = "Difficulty: ${workout.difficulty}"
        holder.daysTextView.text = "Days per week: ${workout.days} days"
        holder.levelUpPointsTextView.text = "Level Up Points: ${workout.levelUpPoints}"

        val weightedWorkoutsAdapter = WeightedWorkoutAdapter(workout.weightedWorkouts)
        holder.weightedWorkoutsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.weightedWorkoutsRecyclerView.adapter = weightedWorkoutsAdapter

        // Set up delete button functionality
        holder.deleteButton.setOnClickListener {
            onDeleteClick(workout)
        }

        // Set up edit button functionality
        holder.editButton.setOnClickListener {
            onEditClick(workout)
        }

        // Set up add button functionality
        holder.addButton.setOnClickListener {
            onAddClick(workout)
        }

        holder.completeButton.setOnClickListener {
            onCompleteClick(workout)
        }
    }

    override fun getItemCount(): Int = workouts.size

    fun updateData(newWorkouts: MutableList<WorkoutModel>) {
        workouts.clear()
        workouts.addAll(newWorkouts)
        notifyDataSetChanged()
    }
}



