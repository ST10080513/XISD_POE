package com.example.xisd_poe

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var currentPasswordEditText: EditText
    private lateinit var updateProfileButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        updateProfileButton = findViewById(R.id.updateProfileButton)

        // Load current user profile
        loadUserProfile()

        // Set the update button click listener
        updateProfileButton.setOnClickListener {
            val currentPassword = currentPasswordEditText.text.toString().trim()
            if (currentPassword.isNotEmpty()) {
                reAuthenticateUser(currentPassword) { success ->
                    if (success) {
                        updateProfile() // Proceed with profile update if re-authentication succeeds
                    } else {
                        Toast.makeText(this, "Re-authentication failed. Please check your current password.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please enter your current password to update profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load user profile data
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Load data from Realtime Database (for name)
            val userId = user.uid
            val userRef = database.getReference("Users").child(userId)

            userRef.child("name").get().addOnSuccessListener {
                nameEditText.setText(it.value.toString())
            }.addOnFailureListener {
                Log.e("ProfileActivity", "Error getting user name", it)
            }

            // Set the current email in the EditText
            emailEditText.setText(user.email)
        }
    }

    // Re-authenticate user
    private fun reAuthenticateUser(currentPassword: String, callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileActivity", "User re-authenticated.")
                    callback(true)
                } else {
                    Log.e("ProfileActivity", "Re-authentication failed.")
                    callback(false)
                }
            }
        }
    }

    // Update profile information
    private fun updateProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Update name in Firebase Realtime Database
            val userId = user.uid
            val userRef = database.getReference("Users").child(userId)
            userRef.child("name").setValue(name).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Name updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error updating name", Toast.LENGTH_SHORT).show()
                }
            }

            // Update email in Firebase Authentication
            if (email.isNotEmpty() && email != user.email) {
                user.updateEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error updating email", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Update password in Firebase Authentication
            if (password.isNotEmpty()) {
                user.updatePassword(password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error updating password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
