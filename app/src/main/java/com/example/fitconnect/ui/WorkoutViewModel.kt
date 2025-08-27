package com.example.fitconnect.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fitconnect.model.UserModel
import com.example.fitconnect.model.WorkoutModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.example.fitconnect.tools.DBTools

class WorkoutViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val _totalXP = MutableLiveData<Int>(0)
    val totalXP: LiveData<Int> get() = _totalXP

    private val _workouts = MutableLiveData<MutableList<WorkoutModel>>()
    val workouts: LiveData<MutableList<WorkoutModel>> get() = _workouts

    init {
        getWorkouts()
    }

    fun getWorkouts() {
        db.collection("workouts").get()
            .addOnSuccessListener { result ->
                val workoutList = result.toObjects(WorkoutModel::class.java)
                _workouts.postValue(workoutList)
            }
            .addOnFailureListener {
                // Handle error
                _workouts.postValue(mutableListOf())
            }
    }

    fun addWorkout(workout: WorkoutModel) {
        val newWorkoutRef = db.collection("workouts").document() // Generate a new document reference
        workout.id = newWorkoutRef.id // Assign the generated ID to the workout
        newWorkoutRef.set(workout)
            .addOnSuccessListener {
                getWorkouts() // Refresh the list after adding a new workout
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun deleteWorkout(workoutId: String) {
        db.collection("workouts").document(workoutId).delete()
            .addOnSuccessListener {
                getWorkouts() // Refresh the list after deleting a workout
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun updateWorkout(workout: WorkoutModel) {
        db.collection("workouts").document(workout.id).set(workout, SetOptions.merge())
            .addOnSuccessListener {
                getWorkouts() // Refresh the list after updating the workout
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun searchWorkouts(query: String) {
        db.collection("workouts")
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .get()
            .addOnSuccessListener { result -> val
                    filteredWorkouts = result.toObjects(WorkoutModel::class.java)
                _workouts.postValue(filteredWorkouts)
            }
            .addOnFailureListener {
                // Handle error
                _workouts.postValue(mutableListOf())
            }
    }
    fun addUserWorkout(context: Context, workout: WorkoutModel) {
        val userId = auth.currentUser?.uid ?: return
        val userWorkoutsRef = db.collection("users").document(userId).collection("workouts")

        // Check if the workout already exists in the user's list
        userWorkoutsRef.document(workout.id).get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Workout already exists, show a message
                Toast.makeText(context, "Workout already in your list", Toast.LENGTH_SHORT).show()
            } else {
                // Add the workout to the user's list
                userWorkoutsRef.document(workout.id).set(workout)
                    .addOnSuccessListener {
                        // Workout added successfully
                        Toast.makeText(context, "Workout added to your list!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener {
                        // Handle error
                        Toast.makeText(context, "Failed to add workout", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    fun completeWorkout(context: Context, workoutId: String) {
        val userId = auth.currentUser?.uid ?: return
        val workoutRef = db.collection("workouts").document(workoutId)
        workoutRef.get()
            .addOnSuccessListener { document ->
                val workout = document.toObject(WorkoutModel::class.java)
                if (workout != null) {
                    // Add level up points to user's XP
                    addXP(context, userId, workout.levelUpPoints, {
                        // Success callback: XP added
                        Toast.makeText(context, "XP added successfully!", Toast.LENGTH_SHORT).show()
                        // Now delete the workout
                        db.collection("workouts").document(workoutId).delete()
                            .addOnSuccessListener {
                                // Success callback: workout deleted
                                Toast.makeText(context, "Workout completed successfully!", Toast.LENGTH_SHORT).show()
                                getWorkouts() // Refresh the list after deleting a workout
                            }
                            .addOnFailureListener { error ->
                                // Failure callback: handle error
                                Toast.makeText(context, "Failed to delete workout: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                    }, { error ->
                        // Failure callback: handle error
                        Toast.makeText(context, "Failed to add XP: ${error.message}", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Log.w("WorkoutViewModel", "Unable to create find workout with id $userId")
                }
            }
            .addOnFailureListener{ error ->
                // Handle error
                Toast.makeText(context, "Failed to fetch workout: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun addXP(context: Context, userId: String, xp: Int, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userRef = db.collection("users").document(userId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentXP = snapshot.getLong("xp")?.toInt() ?: 0
            val newXP = currentXP + xp
            transaction.update(userRef, "xp", newXP)
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun getUserXP(userId: String, callback: (Int) -> Unit) {
        val xpRef = db.collection("users").document(userId).collection("xpData").document("xp")
        xpRef.get()
            .addOnSuccessListener { document ->
                val xp = document.getLong("xp")?.toInt() ?: 0
                callback(xp)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                callback(0)
            }
    }


}

