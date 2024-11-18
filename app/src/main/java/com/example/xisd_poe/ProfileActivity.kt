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
    private lateinit var cityEditText: EditText
    private lateinit var countryEditText: EditText
    private lateinit var streetEditText: EditText
    private lateinit var zipEditText: EditText
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
        cityEditText = findViewById(R.id.cityEditText)
        countryEditText = findViewById(R.id.countryEditText)
        streetEditText = findViewById(R.id.streetEditText)
        zipEditText = findViewById(R.id.zipEditText)
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
            // Load data from Realtime Database (for name and address)
            val userId = user.uid
            val userRef = database.getReference("Users").child(userId)

            userRef.child("name").get().addOnSuccessListener {
                nameEditText.setText(it.value.toString())
            }
            userRef.child("address").get().addOnSuccessListener { snapshot ->
                cityEditText.setText(snapshot.child("city").value.toString())
                countryEditText.setText(snapshot.child("country").value.toString())
                streetEditText.setText(snapshot.child("street").value.toString())
                zipEditText.setText(snapshot.child("zip").value.toString())
            }
            // Set the current email in the EditText
            emailEditText.setText(user.email)
        }
    }

    // Update profile information
    private fun updateProfile() {
        val user = auth.currentUser
        if (user != null) {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            val city = cityEditText.text.toString().trim()
            val country = countryEditText.text.toString().trim()
            val street = streetEditText.text.toString().trim()
            val zip = zipEditText.text.toString().trim()

            // Update name and address in Firebase Realtime Database
            val userId = user.uid
            val userRef = database.getReference("Users").child(userId)
            userRef.child("name").setValue(name)
            userRef.child("address").child("city").setValue(city)
            userRef.child("address").child("country").setValue(country)
            userRef.child("address").child("street").setValue(street)
            userRef.child("address").child("zip").setValue(zip)

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

            // Update email and password in Firebase Authentication
            if (email.isNotEmpty() && email != user.email) {
                user.updateEmail(email)
            }
            if (password.isNotEmpty()) {
                user.updatePassword(password)
            }
        }
    }
    private fun reAuthenticateUser(currentPassword: String, callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            // Get the email and current password credential
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            // Re-authenticate the user
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Re-authentication succeeded, proceed with the profile update
                        callback(true)
                    } else {
                        // Re-authentication failed
                        callback(false)
                    }
                }
        } else {
            callback(false)
        }
    }
}

