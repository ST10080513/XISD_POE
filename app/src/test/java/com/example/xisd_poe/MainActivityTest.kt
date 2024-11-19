package com.example.xisd_poe

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var mockUser: FirebaseUser
    private lateinit var dataSnapshot: DataSnapshot

    private val testEmail = "test@example.com"
    private val testPassword = "password123"
    private val testUserId = "12345"
    private val testUserName = "Test User"

    @Before
    fun setUp() {
        auth = mock(FirebaseAuth::class.java)
        database = mock(FirebaseDatabase::class.java)
        userRef = mock(DatabaseReference::class.java)
        mockUser = mock(FirebaseUser::class.java)
        dataSnapshot = mock(DataSnapshot::class.java)

        // Mock current user and user data
        `when`(auth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(testUserId)
        `when`(database.getReference("Users").child(testUserId)).thenReturn(userRef)
        `when`(dataSnapshot.child("name").getValue(String::class.java)).thenReturn(testUserName)
    }

    @Test
    fun loginSuccessful() {
        // Mock successful sign-in
        `when`(auth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockTask { true })

        // Mock successful database read
        `when`(userRef.get()).thenReturn(mockTask { dataSnapshot })

        val activity = MainActivity()
        activity.auth = auth
        activity.database = database

        val intentCaptor = intentCaptor<HomeActivity>()
        activity.startLoginProcess(testEmail, testPassword, intentCaptor)
        assertEquals(testUserName, intentCaptor.value.getStringExtra("USER_NAME"))
    }

    @Test
    fun loginFailureInvalidEmail() {
        // Mock failure: invalid email format
        val exception = FirebaseAuthException("ERROR_INVALID_EMAIL", "Invalid email format")
        `when`(auth.signInWithEmailAndPassword(testEmail, testPassword))
            .thenReturn(mockTask { false }.apply { exception?.let { setException(it) } })

        val activity = MainActivity()
        activity.auth = auth
        activity.database = database

        activity.startLoginProcess(testEmail, testPassword, null)
        assertEquals("The email address is badly formatted.", activity.getLastError())
    }

    private inline fun <reified T> mockTask(success: Boolean): Task<T> {
        val task = mock(Task::class.java) as Task<T>
        `when`(task.isSuccessful).thenReturn(success)
        return task
    }

    private inline fun <reified T : Intent> intentCaptor(): T {
        val intent = mock(T::class.java)
        `when`(intent.putExtra(anyString(), any())).thenReturn(intent)
        return intent
    }
}
