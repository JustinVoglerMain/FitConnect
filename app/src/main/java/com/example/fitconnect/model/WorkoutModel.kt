package com.example.fitconnect.model

data class WorkoutModel (
    var id: String = "", // Workout ID
    val name: String = "",
    val description: String = "", // Ensure this is defined
    val duration: Int = 0,
    val type: String = "",
    val difficulty: String = "",
    val days: Int = 0,
    val levelUpPoints: Int = 0,
    val weightedWorkouts: List<WeightedWorkout> = listOf()
)


data class WeightedWorkout(
    val name: String = "",
    val reps: Int = 0,
    val sets: Int = 0,
    val weight: Double = 0.0
)

