package com.example.fitconnect

import com.example.fitconnect.ui.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
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
    fun `getCurrentUser should return mock user`() {
        // Call the function in the ViewModel
        val currentUser = viewModel.getCurrentUser()

        // Assert the mock user is returned
        assert(currentUser != null) { "Expected a non-null mock user." }
    }

    @Test
    fun `getCurrentUser should return null if user is not logged in`() {
        // Mock currentUser to return null
        every { FirebaseAuth.getInstance().currentUser } returns null

        // Call the function in the ViewModel
        val currentUser = viewModel.getCurrentUser()

        // Assert the currentUser is null
        assertNull(currentUser)
    }
}
