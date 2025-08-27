package com.example.fitconnect.tools

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R

class WeightedWorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameTextView: TextView = itemView.findViewById(R.id.weighted_workout_name)
    val repsTextView: TextView = itemView.findViewById(R.id.weighted_workout_reps)
    val setsTextView: TextView = itemView.findViewById(R.id.weighted_workout_sets)
    val weightTextView: TextView = itemView.findViewById(R.id.weighted_workout_weight)
}
