package com.example.fitconnect


import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.SmallTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitconnect.activity.CreateAccountActivity
import com.example.fitconnect.activity.LoginActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ProfileInstrumentedTest {

    private val TEST_NAME = "Justin Vogler"
    private val TEST_BIO = "This is my test bio for the app"

    @Before
    fun setup() {
        // Launch CreateAccountActivity before each test
        ActivityScenario.launch(CreateAccountActivity::class.java)
    }


    @Before
    fun login() {
        ActivityScenario.launch(LoginActivity::class.java)
        try {
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
            return
        } catch (e: NoMatchingViewException) {
            // If login screen appears, proceed with login
            onView(withId(R.id.editTextEmail)).perform(typeText("admin@admin.com"), closeSoftKeyboard())
            onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard())
            onView(withId(R.id.fragment_login_button)).perform(click())
        }

        Thread.sleep(2000)

        onView(withId(R.id.nav_profile)).perform(click())
        Thread.sleep(2000)
    }

//    private fun logout() {
//        onView(withId(R.id.settings_button)).perform(click())
//        onView(withId(R.id.logoutBtn)).perform(click())
//        onView(withId(R.id.main_login_button)).perform(click())
//        onView(withId(R.id.editTextEmail)).perform(typeText("admin@admin.com"), closeSoftKeyboard())
//        onView(withId(R.id.editTextPassword)).perform(typeText("password123"), closeSoftKeyboard())
//        onView(withId(R.id.fragment_login_button)).perform(click())
//    }
    @Test
    fun profileSettings_EditNameAndBio() {

        Thread.sleep(2000)
        // Check that the default profile name and bio are displayed
        onView(withId(R.id.userNameTextView)).check(matches(isDisplayed()))
        //onView(withId(R.id.userNameTextView)).check(matches(withText(R.string.firstname_lastname)))

        onView(withId(R.id.userBioTextView)).check(matches(isDisplayed()))
        //onView(withId(R.id.userBioTextView)).check(matches(withText(R.string.bio)))

        // Simulate clicking the edit name button
        onView(withId(R.id.editNameButton)).perform(click())


        // Clear the existing name
        onView(withId(R.id.userNameEditText))
            .perform(clearText(), closeSoftKeyboard())

        // Enter a new name and save
        onView(withId(R.id.userNameEditText)).perform(typeText(TEST_NAME), closeSoftKeyboard())

        onView(withId(R.id.editNameButton)).perform(click())
        Thread.sleep(2000)
        onView(withId(R.id.userNameTextView)).check(matches(withText(TEST_NAME)))


        // Simulate clicking the edit bio button
        onView(withId(R.id.editBioButton)).perform(click())
        // Clear the existing bio
        onView(withId(R.id.userBioEditText))
            .perform(clearText(), closeSoftKeyboard())


        // Enter a new bio and save
        onView(withId(R.id.userBioEditText)).perform(typeText(TEST_BIO), closeSoftKeyboard())

        onView(withId(R.id.editBioButton)).perform(click())
        Thread.sleep(2000)

        // Verify that the changes are reflected in the UI
        onView(withId(R.id.userNameTextView)).check(matches(withText(TEST_NAME)))
        onView(withId(R.id.userBioTextView)).check(matches(withText(TEST_BIO)))

        onView(withId(R.id.nav_home)).perform(click())
    }
}
