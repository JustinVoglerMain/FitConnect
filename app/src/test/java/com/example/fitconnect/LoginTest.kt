package com.example.fitconnect

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.example.fitconnect.ui.LoginViewModel
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        //every { Log.w(any(), any()) } returns 0
        every { Log.v(any(), any()) } returns 0
        // Mock the static FirebaseAuth.getInstance()
        mockkStatic(FirebaseAuth::class)

        // Create a mock of FirebaseAuth and FirebaseUser
        val mockFirebaseAuth: FirebaseAuth = mockk(relaxed = true)
        val mockFirebaseUser: FirebaseUser = mockk(relaxed = true)

        // Mock FirebaseAuth.getInstance() to return the mock instance
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth

        // Mock the currentUser property of FirebaseAuth
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

        // Initialize the ViewModel
        viewModel = LoginViewModel()
    }

    @Test
    fun `signIn should post true when login is successful`() {
        // Arrange
        val email = "admin@admin.com"
        val password = "password123"

        // Mock the Task<AuthResult>
        val mockAuthResult: AuthResult = mockk(relaxed = true)
        val mockTask: Task<AuthResult> = mockk {
            every { isSuccessful } returns true
            every { result } returns mockAuthResult
        }

        // Mock the FirebaseAuth behavior
        every {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        } returns mockTask

        // Mock addOnCompleteListener to simulate task completion
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as OnCompleteListener<AuthResult>
            listener.onComplete(mockTask)
            mockTask
        }

        val observer: Observer<Boolean> = mockk(relaxed = true)
        viewModel.loginStatus.observeForever(observer)

        // Act
        viewModel.signIn(email, password)

        // Assert
        verify { observer.onChanged(true) }
    }

    @Test
    fun `signIn should post false when login fails`() {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"

        // Mock the Task<AuthResult>
        val mockTask: Task<AuthResult> = mockk {
            every { isSuccessful } returns false
        }

        // Mock the FirebaseAuth behavior
        every {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        } returns mockTask

        // Mock addOnCompleteListener to simulate task completion
        every { mockTask.addOnCompleteListener(any()) } answers {
            val listener = it.invocation.args[0] as OnCompleteListener<AuthResult>
            listener.onComplete(mockTask)
            mockTask
        }

        val observer: Observer<Boolean> = mockk(relaxed = true)
        viewModel.loginStatus.observeForever(observer)

        // Act
        viewModel.signIn(email, password)

        // Assert
        verify { observer.onChanged(false) }
    }
}