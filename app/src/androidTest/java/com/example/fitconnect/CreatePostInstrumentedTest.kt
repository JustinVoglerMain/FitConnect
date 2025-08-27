package com.example.fitconnect


import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitconnect.activity.LoginActivity
import com.example.fitconnect.activity.TimelineActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreatePostInstrumentedTest {


    @Before
    fun login() {
        ActivityScenario.launch(LoginActivity::class.java)

        try {
            Thread.sleep(2000)
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))
            return
        } catch (e: NoMatchingViewException) {
            // If login screen appears, proceed with login
            onView(withId(R.id.editTextEmail)).perform(
                typeText("admin@admin.com"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.editTextPassword)).perform(
                typeText("password123"),
                closeSoftKeyboard()
            )
            onView(withId(R.id.fragment_login_button)).perform(click())
        }

        Thread.sleep(2000)
    }


    @Test
    fun testCreatePostWithTextContent() {
        ActivityScenario.launch(TimelineActivity::class.java)
        onView(withId(R.id.createPostButton)).perform(click())

        val postContent = "This is a test post"
        onView(withId(R.id.postContentEditText))
            .perform(typeText(postContent), closeSoftKeyboard())

        onView(withId(R.id.postButton)).check(matches(isEnabled()))
        onView(withId(R.id.postButton)).perform(click())


        Thread.sleep(2000)

        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))

        onView(withId(R.id.recyclerView)).check(matches(hasDescendant(withText(postContent))))
    }
}