package com.example.xisd_poe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

// Define the adapter class
class CartAdapter(
    private val context: Context,
    private val cartItems: ArrayList<Pair<String, Double>>,
    private val removeItemCallback: (Int) -> Unit // Callback function to remove item from cart
) : BaseAdapter() {

    // Returns the total number of items in the cart
    override fun getCount(): Int {
        return cartItems.size
    }

    // Returns the item at a specific position
    override fun getItem(position: Int): Any {
        return cartItems[position]
    }

    // Returns the item ID at a specific position (usually, position itself)
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    // Returns the view for each item in the list
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the custom layout for each cart item (cart_item.xml)
        val itemView = convertView ?: LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false)

        // Find views in the custom layout
        val itemName = itemView.findViewById<TextView>(R.id.tvCartItemName)
        val btnRemoveItem = itemView.findViewById<Button>(R.id.btnRemoveItem)

        // Get the current cart item (name and price)
        val item = cartItems[position]

        // Set the item name and price in the TextView
        itemName.text = "${item.first} - R${item.second}"

        // Handle the "Remove" button click
        btnRemoveItem.setOnClickListener {
            // Show a confirmation dialog before removing the item
            AlertDialog.Builder(context)
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove this item?")
                .setPositiveButton("Yes") { _, _ ->
                    // Call the callback function to remove the item from the cart
                    removeItemCallback(position)
                }
                .setNegativeButton("No", null)
                .show()
        }

        return itemView
    }
}
