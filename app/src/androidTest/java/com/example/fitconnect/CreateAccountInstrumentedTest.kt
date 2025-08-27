package com.example.fitconnect

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitconnect.activity.CreateAccountActivity
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateAccountInstrumentedTest {

    private val TEST_FIRST_NAME = "Justin"
    private val TEST_LAST_NAME = "Vogler"
    private val TEST_EMAIL = "test@test.com"
    private val TEST_EMAIL_REAL = "admin@admin.com"
    private val TEST_PASSWORD = "password123"
    private val MISMATCH_PASSWORD = "password321"

    @Before
    fun setup() {
        // Launch CreateAccountActivity before each test
        ActivityScenario.launch(CreateAccountActivity::class.java)
    }


    @Test
    fun createAccount_SuccessfulCreation() {
        // Enter first name
        onView(withId(R.id.etFirstName)).perform(typeText(TEST_FIRST_NAME), closeSoftKeyboard())
        onView(withId(R.id.etFirstName)).check(matches(withText(TEST_FIRST_NAME)))

        // Enter last name
        onView(withId(R.id.etLastName)).perform(typeText(TEST_LAST_NAME), closeSoftKeyboard())
        onView(withId(R.id.etLastName)).check(matches(withText(TEST_LAST_NAME)))

        // Enter email
        onView(withId(R.id.etEmail)).perform(typeText(TEST_EMAIL), closeSoftKeyboard())
        onView(withId(R.id.etEmail)).check(matches(withText(TEST_EMAIL)))

        // Enter password
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.etPassword)).check(matches(withText(TEST_PASSWORD)))

        // Confirm password
        onView(withId(R.id.etConfirmPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard())
        onView(withId(R.id.etConfirmPassword)).check(matches(withText(TEST_PASSWORD)))

        // Click "Create Account" button
        onView(withId(R.id.btnCreateAccount)).perform(click())

        // Wait for dialog to appear
        Thread.sleep(2000) // Use IdlingResource for more robust handling

        // Check for success dialog message
        onView(withText("Account has been created successfully"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun createAccount_PasswordMismatch() {
        // Enter first name
        onView(withId(R.id.etFirstName)).perform(typeText(TEST_FIRST_NAME), closeSoftKeyboard())

        // Enter last name
        onView(withId(R.id.etLastName)).perform(typeText(TEST_LAST_NAME), closeSoftKeyboard())

        // Enter email
        onView(withId(R.id.etEmail)).perform(typeText(TEST_EMAIL), closeSoftKeyboard())

        // Enter password
        onView(withId(R.id.etPassword)).perform(typeText(TEST_PASSWORD), closeSoftKeyboard())

        // Confirm password with mismatch
        onView(withId(R.id.etConfirmPassword)).perform(
            typeText(MISMATCH_PASSWORD),
            closeSoftKeyboard()
        )

        // Click "Create Account" button
        onView(withId(R.id.btnCreateAccount)).perform(click())

        // Check for error dialogue message
        onView(withText("There was an error creating your account. Please verify you have entered the correct information and try again."))
            .check(matches(isDisplayed()))
    }

    @Test
    fun createAccount_EmptyFields() {
        // Leave all fields empty
        onView(withId(R.id.btnCreateAccount)).perform(click())

        // Check for error dialogue message
        onView(withText("There was an error creating your account. Please verify you have entered the correct information and try again."))
            .check(matches(isDisplayed()))
    }

    @Test
    fun returnToHomepage() {
        ActivityScenario.launch(CreateAccountActivity::class.java).use { scenario ->
            // Perform button click
            onView(withId(R.id.btnReturnHomepage)).perform(click())

            // Check if activity is finishing
            scenario.onActivity { activity ->
                assertTrue("Activity should be finishing", activity.isFinishing)
            }
        }
    }

}