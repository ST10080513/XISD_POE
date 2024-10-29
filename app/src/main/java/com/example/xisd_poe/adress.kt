package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class adress : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var btnSaveAddress: Button
    private lateinit var etCity: EditText
    private lateinit var etCountry: EditText
    private lateinit var etStreet: EditText
    private lateinit var etZip: EditText

    private lateinit var cartItems: ArrayList<Pair<String, Double>>
    private var totalPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adress)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        btnSaveAddress = findViewById(R.id.btnSaveAddress)
        etCity = findViewById(R.id.cityInput)
        etCountry = findViewById(R.id.countryInput)
        etStreet = findViewById(R.id.streetInput)
        etZip = findViewById(R.id.zipInput)

        cartItems = intent.getSerializableExtra("cartItems") as ArrayList<Pair<String, Double>>
        totalPrice = intent.getDoubleExtra("totalPrice", 0.0)

        // Check if user address exists
        checkUserAddress()

        btnSaveAddress.setOnClickListener {
            saveAddressAndProceed()
        }
    }

    private fun checkUserAddress() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val addressRef = database.getReference("Users").child(userId).child("address")
            addressRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Address exists, redirect to paystoreActivity
                        redirectToPaystore()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@adress, "Error checking address", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun saveAddressAndProceed() {
        val city = etCity.text.toString()
        val country = etCountry.text.toString()
        val street = etStreet.text.toString()
        val zip = etZip.text.toString()

        if (city.isEmpty() || country.isEmpty() || street.isEmpty() || zip.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val addressRef = database.getReference("Users").child(userId).child("address")
            val addressData = mapOf(
                "city" to city,
                "country" to country,
                "street" to street,
                "zip" to zip
            )

            addressRef.setValue(addressData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show()
                    redirectToPaystore()
                } else {
                    Toast.makeText(this, "Failed to save address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun redirectToPaystore() {
        val intent = Intent(this, paystoreActivity::class.java)
        intent.putExtra("cartItems", cartItems)
        intent.putExtra("totalPrice", totalPrice)
        startActivity(intent)
        finish() // Close the address activity
    }
}
