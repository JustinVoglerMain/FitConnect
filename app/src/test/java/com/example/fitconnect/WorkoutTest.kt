package com.example.fitconnect

import com.example.fitconnect.model.WorkoutModel
import com.example.fitconnect.model.WeightedWorkout
import org.junit.Assert.*
import org.junit.Test

class WorkoutTest{
    @Test
    fun `test WorkoutModel default values`() {
        val workout = WorkoutModel()

        assertEquals("", workout.id)
        assertEquals("", workout.name)
        assertEquals("", workout.description)
        assertEquals(0, workout.duration)
        assertEquals("", workout.type)
        assertEquals("", workout.difficulty)
        assertEquals(0, workout.days)
        assertEquals(0, workout.levelUpPoints)
        assertTrue(workout.weightedWorkouts.isEmpty())
    }

    @Test
    fun `test WorkoutModel custom values`() {
        val weightedWorkout = WeightedWorkout(name = "Bench Press", reps = 10, sets = 3, weight = 50.0)
        val workout = WorkoutModel(
            id = "workout123",
            name = "Full Body Workout",
            description = "A full-body workout program",
            duration = 60,
            type = "Strength",
            difficulty = "Intermediate",
            days = 5,
            levelUpPoints = 150,
            weightedWorkouts = listOf(weightedWorkout)
        )

        assertEquals("workout123", workout.id)
        assertEquals("Full Body Workout", workout.name)
        assertEquals("A full-body workout program", workout.description)
        assertEquals(60, workout.duration)
        assertEquals("Strength", workout.type)
        assertEquals("Intermediate", workout.difficulty)
        assertEquals(5, workout.days)
        assertEquals(150, workout.levelUpPoints)
        assertEquals(1, workout.weightedWorkouts.size)
        assertEquals(weightedWorkout, workout.weightedWorkouts[0])
    }

    @Test
    fun `test WeightedWorkout default values`() {
        val weightedWorkout = WeightedWorkout()

        assertEquals("", weightedWorkout.name)
        assertEquals(0, weightedWorkout.reps)
        assertEquals(0, weightedWorkout.sets)
        assertEquals(0.0, weightedWorkout.weight, 0.0)
    }

    @Test
    fun `test WeightedWorkout custom values`() {
        val weightedWorkout = WeightedWorkout(name = "Deadlift", reps = 8, sets = 4, weight = 70.5)

        assertEquals("Deadlift", weightedWorkout.name)
        assertEquals(8, weightedWorkout.reps)
        assertEquals(4, weightedWorkout.sets)
        assertEquals(70.5, weightedWorkout.weight, 0.0)
    }

    @Test
    fun `test adding WeightedWorkouts to WorkoutModel`() {
        val workout = WorkoutModel()
        val weightedWorkout1 = WeightedWorkout(name = "Squat", reps = 12, sets = 4, weight = 60.0)
        val weightedWorkout2 = WeightedWorkout(name = "Pull-up", reps = 10, sets = 3, weight = 0.0)

        val updatedWorkout = workout.copy(weightedWorkouts = listOf(weightedWorkout1, weightedWorkout2))

        assertEquals(2, updatedWorkout.weightedWorkouts.size)
        assertEquals(weightedWorkout1, updatedWorkout.weightedWorkouts[0])
        assertEquals(weightedWorkout2, updatedWorkout.weightedWorkouts[1])
    }
}

