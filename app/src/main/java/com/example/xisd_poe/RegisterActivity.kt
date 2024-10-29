package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)

        btnCreateAccount.setOnClickListener {
            val email = findViewById<EditText>(R.id.etRegisterEmail).text.toString()
            val password = findViewById<EditText>(R.id.etRegisterPassword).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword).text.toString()
            val name = findViewById<EditText>(R.id.etRegisterName).text.toString()
            val phone = findViewById<EditText>(R.id.etRegisterPhone).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && name.isNotEmpty() && phone.isNotEmpty()) {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    val userRef = database.getReference("Users").child(userId)
                                    val user = mapOf(
                                        "name" to name,
                                        "email" to email,
                                        "phone" to phone  // Storing the phone number
                                    )
                                    userRef.setValue(user).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            // Registration successful
                                            val intent = Intent(this, HomeActivity::class.java)
                                            intent.putExtra("USER_NAME", name)
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else {
                                // Handle Firebase registration error
                                handleFirebaseError(task.exception)
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleFirebaseError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            when (exception.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                    Toast.makeText(this, "This email is already in use. Please try another.", Toast.LENGTH_SHORT).show()
                }
                "ERROR_WEAK_PASSWORD" -> {
                    Toast.makeText(this, "The password is too weak. Please choose a stronger password.", Toast.LENGTH_SHORT).show()
                }
                "ERROR_INVALID_EMAIL" -> {
                    Toast.makeText(this, "The email address is badly formatted.", Toast.LENGTH_SHORT).show()
                }
                "ERROR_NETWORK_REQUEST_FAILED" -> {
                    Toast.makeText(this, "Network error. Please check your connection and try again.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "An unknown error occurred.", Toast.LENGTH_SHORT).show()
        }
    }
}
