package com.example.fitconnect.tools

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R

class WorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameTextView: TextView = itemView.findViewById(R.id.workout_name)
    val descriptionTextView: TextView = itemView.findViewById(R.id.workout_description)
    val durationTextView: TextView = itemView.findViewById(R.id.workout_duration)
    val typeTextView: TextView = itemView.findViewById(R.id.workout_type)
    val difficultyTextView: TextView = itemView.findViewById(R.id.workout_difficulty)
    val daysTextView: TextView = itemView.findViewById(R.id.workout_days)
    val levelUpPointsTextView: TextView = itemView.findViewById(R.id.workout_level_up_points)
    val weightedWorkoutsRecyclerView: RecyclerView = itemView.findViewById(R.id.weighted_workouts_recycler_view)
    val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    val editButton: Button = itemView.findViewById(R.id.edit_button)
    val addButton: Button = itemView.findViewById(R.id.add_button)
    val completeButton: Button = itemView.findViewById(R.id.complete_button)
}
