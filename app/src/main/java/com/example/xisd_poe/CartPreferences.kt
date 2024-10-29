package com.example.xisd_poe

import android.content.Context

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)

    // Save cart items to SharedPreferences
    fun saveCartItems(cartItems: List<Pair<String, Double>>) {
        val editor = prefs.edit()
        val gson = Gson()
        val json = gson.toJson(cartItems)
        editor.putString("cart_items", json)
        editor.apply() // Save changes asynchronously
    }

    // Retrieve cart items from SharedPreferences
    fun getCartItems(): ArrayList<Pair<String, Double>> {
        val gson = Gson()
        val json = prefs.getString("cart_items", null)
        val type = object : TypeToken<ArrayList<Pair<String, Double>>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            ArrayList() // Return an empty cart if no data found
        }
    }

    // Clear cart items in SharedPreferences
    fun clearCart() {
        val editor = prefs.edit()
        editor.remove("cart_items")
        editor.apply() // Save changes asynchronously
    }
}
