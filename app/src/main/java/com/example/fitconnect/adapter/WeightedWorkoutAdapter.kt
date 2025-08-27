package com.example.fitconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitconnect.R
import com.example.fitconnect.model.WeightedWorkout

class WeightedWorkoutAdapter(private val weightedWorkouts: List<WeightedWorkout>) :
    RecyclerView.Adapter<WeightedWorkoutAdapter.WeightedWorkoutViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightedWorkoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_weighted_workout, parent, false)
        return WeightedWorkoutViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightedWorkoutViewHolder, position: Int) {
        holder.bind(weightedWorkouts[position])
    }

    override fun getItemCount(): Int = weightedWorkouts.size

    class WeightedWorkoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.weighted_workout_name)
        private val repsTextView: TextView = itemView.findViewById(R.id.weighted_workout_reps)
        private val setsTextView: TextView = itemView.findViewById(R.id.weighted_workout_sets)
        private val weightTextView: TextView = itemView.findViewById(R.id.weighted_workout_weight)

        fun bind(weightedWorkout: WeightedWorkout) {
            nameTextView.text = weightedWorkout.name
            repsTextView.text = "Reps: ${weightedWorkout.reps}"
            setsTextView.text = "Sets: ${weightedWorkout.sets}"
            weightTextView.text = "Weight: ${weightedWorkout.weight} lbs"
        }
    }
}
