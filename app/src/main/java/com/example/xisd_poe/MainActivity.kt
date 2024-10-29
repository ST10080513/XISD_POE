package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize buttons
        val btnLogin = findViewById<Button>(R.id.login_button)
        val btnRegister = findViewById<Button>(R.id.register_button)

        // Register button function
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Login button functionality
        btnLogin.setOnClickListener {

            val email = findViewById<EditText>(R.id.email).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Fetch the user's name from the database
                            val userId = auth.currentUser?.uid
                            if (userId != null) {
                                val userRef = database.getReference("Users").child(userId)
                                userRef.get().addOnCompleteListener { dataSnapshotTask ->
                                    if (dataSnapshotTask.isSuccessful) {
                                        val dataSnapshot = dataSnapshotTask.result
                                        val userName = dataSnapshot.child("name").getValue(String::class.java)

                                        if (userName != null) {
                                            // Pass the user's name to the HomeActivity
                                            val intent = Intent(this, HomeActivity::class.java)
                                            intent.putExtra("USER_NAME", userName)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(this, "Failed to retrieve name", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            // Login failed, handle the exception
                            handleFirebaseAuthError(task.exception)
                        }
                    }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to handle Firebase authentication errors
    private fun handleFirebaseAuthError(exception: Exception?) {
        // Log the exception for debugging purposes
        Log.e("FirebaseAuthError", "Error: ${exception?.message}")

        if (exception is FirebaseAuthException) {
            when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> {
                    showErrorMessage("The email address is badly formatted.")
                }
                "ERROR_WRONG_PASSWORD" -> {
                    showErrorMessage("The password is incorrect.")
                }
                "ERROR_USER_NOT_FOUND" -> {
                    showErrorMessage("No account found with this email.")
                }
                "ERROR_USER_DISABLED" -> {
                    showErrorMessage("This account has been disabled.")
                }
                "ERROR_NETWORK_REQUEST_FAILED" -> {
                    showErrorMessage("Network error. Please check your connection.")
                }
                else -> {
                    showErrorMessage("Authentication failed. Please try again.")
                }
            }
        } else if (exception != null) {
            // If it's not a FirebaseAuthException, show the raw exception message
            showErrorMessage(exception.message ?: "Authentication failed. Please try again.")
        } else {
            showErrorMessage("An unknown error occurred. Please try again.")
        }
    }

    // Function to display error messages
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
