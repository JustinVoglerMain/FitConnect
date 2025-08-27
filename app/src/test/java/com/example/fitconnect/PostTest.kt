package com.example.fitconnect

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.fitconnect.model.PostModel
import com.example.fitconnect.tools.DBTools
import com.example.fitconnect.ui.CreatePostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

class PostTests {

    // Variables for PostModel tests



    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    private lateinit var postModel: PostModel

    // Variables for CreatePostFragment tests
    private lateinit var dbTools: DBTools
    private lateinit var createPostViewModel: CreatePostViewModel



    @Before
    fun setUp() {
//        // Initialize objects for PostModel tests
        postModel = PostModel()
//
//        // Initialize mocks for CreatePostFragment tests
        dbTools = mockk(relaxed = true)
        createPostViewModel = CreatePostViewModel()

        mockkStatic(FirebaseAuth::class)

        // Create a mock of FirebaseAuth and FirebaseUser
        val mockFirebaseAuth: FirebaseAuth = mockk(relaxed = true)
        val mockFirebaseUser: FirebaseUser = mockk(relaxed = true)

        // Mock FirebaseAuth.getInstance() to return the mock instance
        every { FirebaseAuth.getInstance() } returns mockFirebaseAuth

        // Mock the currentUser property of FirebaseAuth
        every { mockFirebaseAuth.currentUser } returns mockFirebaseUser

    }


    @Test
    fun `test PostModel default values`() {
        assertEquals("", postModel.postId)
        assertEquals("", postModel.posterUID)
        assertEquals("", postModel.posterName)
        assertNotNull(postModel.date)
        assertEquals("", postModel.content)
        assertEquals("", postModel.imgURL)
        assertEquals("", postModel.deleteHash)
        assertEquals("0", postModel.likesCount)
        assertEquals("0", postModel.commentsCount)
        assertFalse(postModel.isCurrentUser)
    }

    @Test
    fun `test PostModel with custom values`() {
        val date = Date()
        val customPost = PostModel(
            postId = "123",
            posterUID = "user123",
            posterName = "John Doe",
            date = date,
            content = "This is a test post",
            imgURL = "http://example.com/image.jpg",
            deleteHash = "deleteHash123",
            likesCount = "100",
            commentsCount = "10",
            isCurrentUser = true
        )

        assertEquals("123", customPost.postId)
        assertEquals("user123", customPost.posterUID)
        assertEquals("John Doe", customPost.posterName)
        assertEquals(date, customPost.date)
        assertEquals("This is a test post", customPost.content)
        assertEquals("http://example.com/image.jpg", customPost.imgURL)
        assertEquals("deleteHash123", customPost.deleteHash)
        assertEquals("100", customPost.likesCount)
        assertEquals("10", customPost.commentsCount)
        assertTrue(customPost.isCurrentUser)
    }
    @Test
    fun `test post content is observed correctly`() {
        val observer: Observer<String> = mockk(relaxed = true)
        createPostViewModel.postContent.observeForever(observer)

        // Act
        createPostViewModel.setPostContent("Test post content")

        // Assert
        assertEquals("Test post content", createPostViewModel.postContent.value)
        verify { observer.onChanged("Test post content") }
    }


}
