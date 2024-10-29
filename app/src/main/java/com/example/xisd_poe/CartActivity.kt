package com.example.xisd_poe

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class CartActivity : AppCompatActivity() {

    private lateinit var cartItems: ArrayList<Pair<String, Double>>
    private lateinit var adapter: CartAdapter
    private lateinit var tvTotalPrice: TextView
    private lateinit var cartPreferences: CartPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        // Initialize CartPreferences to manage persistent storage
        cartPreferences = CartPreferences(this)

        // Load saved cart items from SharedPreferences
        cartItems = cartPreferences.getCartItems()

        // Get the ListView and TextView references
        val cartListView = findViewById<ListView>(R.id.cartListView)
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        val btnProceedToPayment = findViewById<Button>(R.id.btnProceedToPayment)

        // Set up the adapter with a remove item callback
        adapter = CartAdapter(this, cartItems) { position ->
            removeItemFromCart(position)
        }
        cartListView.adapter = adapter

        // Update total price when the activity starts
        updateTotalPrice()

        // Handle "Proceed to Payment" button click
        btnProceedToPayment.setOnClickListener {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Your cart is empty. Add items to proceed.", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("cartItems", cartItems) // Pass cart items
                val totalPrice = cartItems.sumOf { it.second }
                intent.putExtra("totalPrice", totalPrice) // Pass total price
                startActivity(intent)
            }
        }
    }

    // Remove item from the cart and update SharedPreferences
    private fun removeItemFromCart(position: Int) {
        cartItems.removeAt(position) // Remove the item from the list
        cartPreferences.saveCartItems(cartItems) // Save updated cart to SharedPreferences
        adapter.notifyDataSetChanged() // Notify the adapter to refresh the ListView
        updateTotalPrice() // Update the total price
    }

    // Update the total price in the TextView
    private fun updateTotalPrice() {
        val totalPrice = cartItems.sumOf { it.second }
        tvTotalPrice.text = "Total Price: R$totalPrice"
    }

    override fun onPause() {
        super.onPause()
        // Save cart items to SharedPreferences when activity is paused
        cartPreferences.saveCartItems(cartItems)
    }

    override fun onResume() {
        super.onResume()
        // Load cart items from SharedPreferences when activity is resumed
        cartItems.clear()
        cartItems.addAll(cartPreferences.getCartItems())
        adapter.notifyDataSetChanged() // Notify the adapter to refresh the ListView
        updateTotalPrice() // Update total price
    }
}
