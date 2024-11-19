package com.example.xisd_poe

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import kotlinx.coroutines.runBlocking
import com.google.firebase.auth.AuthResult
import org.mockito.Mockito.*
import org.junit.Assert.assertEquals



class MainActivityTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDatabase: FirebaseDatabase
    private lateinit var mockUserRef: DatabaseReference
    private lateinit var mockSnapshot: DataSnapshot

    @Before
    fun setUp() {
        // Initialize mocks
        mockAuth = mock(FirebaseAuth::class.java)
        mockDatabase = mock(FirebaseDatabase::class.java)
        mockUserRef = mock(DatabaseReference::class.java)
        mockSnapshot = mock(DataSnapshot::class.java)

        // Mock FirebaseDatabase references
        `when`(mockDatabase.getReference("Users")).thenReturn(mockUserRef)
        `when`(mockUserRef.child("12345")).thenReturn(mockUserRef)

        // Mock DataSnapshot behavior
        val mockNameSnapshot = mock(DataSnapshot::class.java)
        `when`(mockSnapshot.child("name")).thenReturn(mockNameSnapshot)
        `when`(mockNameSnapshot.getValue(String::class.java)).thenReturn("Test User")

        // Simulate successful data retrieval
        val mockTask = Tasks.forResult(mockSnapshot)
        `when`(mockUserRef.get()).thenReturn(mockTask)
    }

    @Test
    fun testLoginWithValidCredentials() {
        // Mock current user ID in FirebaseAuth
        val mockFirebaseUser = mock(FirebaseUser::class.java)
        `when`(mockAuth.currentUser).thenReturn(mockFirebaseUser)
        `when`(mockFirebaseUser.uid).thenReturn("12345")

        // Retrieve user data from the database
        val task = mockUserRef.get()
        val userName = task.result?.child("name")?.getValue(String::class.java)

        // Verify the retrieved user name matches the expected value
        assertEquals("Test User", userName)
    }

    @Test
    fun testLoginWithInvalidEmail() = runBlocking {
        // Mock FirebaseAuth sign-in to return a failed Task with an exception
        val exception = Exception("Invalid email format")
        val failedTask = Tasks.forException<AuthResult>(exception)
        `when`(mockAuth.signInWithEmailAndPassword("invalid", "password123"))
            .thenReturn(failedTask)

        // Attempt to log in with an invalid email
        try {
            // Await the task result (this is a coroutine-suspend function)
            val taskResult = mockAuth.signInWithEmailAndPassword("invalid", "password123")
            taskResult.await() // This will throw an exception if the task fails

            // If it succeeds, we should not reach here
            assert(false) { "Expected exception to be thrown" }
        } catch (e: Exception) {
            // Verify that the exception message matches the expected error
            assertEquals("Invalid email format", e.message)
        }
    }



    @Test
    fun testFailedDatabaseRead() {
        // Simulate a failed task
        val failedTask = Tasks.forException<DataSnapshot>(Exception("Database read failed"))
        `when`(mockUserRef.get()).thenReturn(failedTask)

        // Attempt to retrieve data
        val taskResult = mockUserRef.get()

        // Verify that the task fails
        assertEquals(false, taskResult.isSuccessful)
    }
}
