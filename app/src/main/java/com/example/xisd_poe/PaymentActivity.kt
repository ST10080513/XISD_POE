package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val btnPayAtStore = findViewById<Button>(R.id.btnPayAtStore)
        val btnPayOnline = findViewById<Button>(R.id.btnPayOnline)

        btnPayAtStore.setOnClickListener {

            val cartItems = intent.getSerializableExtra("cartItems") as ArrayList<Pair<String, Double>>
            val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
            val intent = Intent(this, payActivity::class.java)
            intent.putExtra("cartItems", cartItems)  // cartItems should be a serializable ArrayList<Pair<String, Double>>
            intent.putExtra("totalPrice", totalPrice)
            startActivity(intent)

        }

        btnPayOnline.setOnClickListener {

            // Get the cart items and total price passed from CartActivity
            val cartItems = intent.getSerializableExtra("cartItems") as ArrayList<Pair<String, Double>>
            val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)
            val intent = Intent(this, adress::class.java)
            intent.putExtra("cartItems", cartItems)  // cartItems should be a serializable ArrayList<Pair<String, Double>>
            intent.putExtra("totalPrice", totalPrice)
            startActivity(intent)

        }

    }
}
