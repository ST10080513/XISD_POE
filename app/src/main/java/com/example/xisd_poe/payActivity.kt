package com.example.xisd_poe

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import android.util.Base64
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import com.google.firebase.auth.FirebaseAuth

class payActivity : AppCompatActivity() {

    private lateinit var tvTotalPrice: TextView
    private lateinit var ivQRCode: ImageView
    private lateinit var tblCartItems: TableLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay)

        // Get references to views
        tvTotalPrice = findViewById(R.id.tvTotalPrice)
        ivQRCode = findViewById(R.id.ivQRCode)
        tblCartItems = findViewById(R.id.tblCartItems)
        val btnGenerateQRCode = findViewById<Button>(R.id.btnGenerateQRCode)

        // Get cart items and total price from intent
        val cartItems = intent.getSerializableExtra("cartItems") as ArrayList<Pair<String, Double>>
        val totalPrice = intent.getDoubleExtra("totalPrice", 0.0)

        // Display total price
        tvTotalPrice.text = "Total Price: R$totalPrice"

        // Populate the table with cart items
        cartItems.forEach { item ->
            val row = TableRow(this)
            val nameCell = TextView(this)
            val priceCell = TextView(this)

            nameCell.text = item.first
            priceCell.text = "R${item.second}"

            row.addView(nameCell)
            row.addView(priceCell)
            tblCartItems.addView(row)
        }

        // Generate QR Code button
        btnGenerateQRCode.setOnClickListener {
            generateQRCode(cartItems, totalPrice)
        }
    }

// Inside payActivity.kt

    private fun generateQRCode(cartItems: ArrayList<Pair<String, Double>>, totalPrice: Double) {
        try {
            val qrData = buildQRCodeData(cartItems, totalPrice)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400)

            // Display the generated QR code
            ivQRCode.setImageBitmap(bitmap)
            Toast.makeText(this, "QR Code generated successfully!", Toast.LENGTH_SHORT).show()

            // Convert QR code bitmap to Base64 and save to Firebase
            saveQRCodeToFirebase(bitmap, cartItems, totalPrice)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to generate QR Code.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveQRCodeToFirebase(qrCodeBitmap: Bitmap, cartItems: ArrayList<Pair<String, Double>>, totalPrice: Double) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance()
        val qrCodeRef = database.getReference("Users").child(userId).child("qrCodes").child(System.currentTimeMillis().toString())

        // Convert Bitmap to Base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val qrCodeBase64 = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

        // Prepare data to save
        val productsList = cartItems.map { mapOf("name" to it.first, "price" to it.second) }
        val qrCodeData = mapOf(
            "products" to productsList,
            "totalAmount" to totalPrice,
            "qrCode" to qrCodeBase64
        )

        // Save to Firebase
        qrCodeRef.setValue(qrCodeData)
            .addOnSuccessListener {
                Toast.makeText(this, "QR Code saved to Firebase under user!", Toast.LENGTH_SHORT).show()
                clearCart()

                // Redirect to ShopActivity
                redirectToShop()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save QR Code to Firebase.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun clearCart() {
        // Assuming CartPreferences is similar to what you used in CartActivity
        val cartPreferences = CartPreferences(this)
        cartPreferences.saveCartItems(arrayListOf()) // Save an empty cart to SharedPreferences

    }

    private fun redirectToShop() {
        val intent = Intent(this, ShopActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clears the activity stack
        startActivity(intent)
        finish()
    }
    // Helper method to format QR Code data
    private fun buildQRCodeData(cartItems: ArrayList<Pair<String, Double>>, totalPrice: Double): String {
        val itemDetails = cartItems.joinToString(separator = ";") { "${it.first}:R${it.second}" }
        return "Items:[$itemDetails];Total:R$totalPrice"
    }
}
