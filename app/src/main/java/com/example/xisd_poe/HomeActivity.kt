package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Get the user's name from the intent
        val userName = intent.getStringExtra("USER_NAME")

        // Display the user's name
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome, $userName!"

        // Initialize the Shop button
        val btnShop = findViewById<Button>(R.id.btnShop)

        // Set an OnClickListener to navigate to the ShopActivity
        btnShop.setOnClickListener {
            val intent = Intent(this, ShopActivity::class.java)
            startActivity(intent)
        }
    }
}
